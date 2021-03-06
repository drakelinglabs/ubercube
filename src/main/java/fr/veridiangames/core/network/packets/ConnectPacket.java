/*
 * Copyright (C) 2016 Team Ubercube
 *
 * This file is part of Ubercube.
 *
 *     Ubercube is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Ubercube is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Ubercube.  If not, see http://www.gnu.org/licenses/.
 */

package fr.veridiangames.core.network.packets;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.veridiangames.core.GameCore;
import fr.veridiangames.core.game.entities.Entity;
import fr.veridiangames.core.game.entities.components.ECName;
import fr.veridiangames.core.game.entities.components.ECNetwork;
import fr.veridiangames.core.game.entities.components.EComponent;
import fr.veridiangames.core.game.entities.player.NetworkedPlayer;
import fr.veridiangames.core.game.entities.player.Player;
import fr.veridiangames.core.game.entities.player.ServerPlayer;
import fr.veridiangames.core.maths.Mathf;
import fr.veridiangames.core.maths.Quat;
import fr.veridiangames.core.maths.Vec3;
import fr.veridiangames.core.maths.Vec4i;
import fr.veridiangames.core.network.NetworkableClient;
import fr.veridiangames.core.network.NetworkableServer;
import fr.veridiangames.core.utils.DataBuffer;
import fr.veridiangames.core.game.modes.GameMode;
import fr.veridiangames.core.utils.Log;

/**
 * Created by Marccspro on 26 f�vr. 2016.
 */
public class ConnectPacket extends Packet
{
	private int id;
	private String name;
	private Vec3 position;
	private Quat rotation;
	private String version;
	private long seed;

	public ConnectPacket()
	
	{
		super(CONNECT);
	}
	
	public ConnectPacket(Player player)
	{
		super(CONNECT);
		Log.println("Your trying to connect with id: " + player.getID());
		data.put(player.getID());
		data.put(player.getName());
		
		data.put(player.getPosition().x);
		data.put(player.getPosition().y);
		data.put(player.getPosition().z);
		
		data.put(player.getRotation().x);
		data.put(player.getRotation().y);
		data.put(player.getRotation().z);
		data.put(player.getRotation().w);

		data.put(GameCore.GAME_SUB_VERSION);

		data.put((long) 0);

		data.flip();
	}
	
	public ConnectPacket(ConnectPacket packet)
	{
		super(CONNECT);
		data.put(packet.id);
		data.put(packet.name);
		
		data.put(packet.position.x);
		data.put(packet.position.y);
		data.put(packet.position.z);
		
		data.put(packet.rotation.x);
		data.put(packet.rotation.y);
		data.put(packet.rotation.z);
		data.put(packet.rotation.w);

		data.put(packet.version);

		data.put(packet.seed);
		
		data.flip();
	}

	public void read(DataBuffer data)
	{
		id = data.getInt();
		name = data.getString();
		position = new Vec3(data.getFloat(), data.getFloat(), data.getFloat());
		rotation = new Quat(data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat());
		version = data.getString();
		seed = data.getLong();
	}

	public void process(NetworkableServer server, InetAddress address, int port)
	{
		seed = server.getCore().getGame().getData().getWorldGen().getSeed();
		ServerPlayer connectPlayer = new ServerPlayer(id, name, position, rotation, address.getHostName(), port);
		connectPlayer.setDead(true);
		server.getCore().getGame().spawn(connectPlayer);

		server.getTcp().getClient(address, port).setID(id);

		server.log(name + " just connected !");
		server.tcpSendToAll(new ConnectPacket(this));

		/* SENDING MULTIPLE PACKETS TO AVOID READ OVERFLOW OF 512 */
		int modifiedBlocksSize = server.getCore().getGame().getWorld().getModifiedBlocks().size();
		int packetCount = (int) ((float) (modifiedBlocksSize * 16) / (Packet.MAX_SIZE - 50)) + 2;
		List<Vec4i> currentData = GameCore.getInstance().getGame().getWorld().getModifiedBlocks();
		int count = (int) ((float) modifiedBlocksSize / (float) packetCount);

//		System.out.println("Modified block size: " + modifiedBlocksSize);
//		System.out.println("Modified block size(Bytes): " + modifiedBlocksSize * 4 * 4);
//		System.out.println("Packet max size: " + Packet.MAX_SIZE);
//		System.out.println("Num packets: " + packetCount);

		boolean finished = false;
		for (int i = 0; i < packetCount + 16; i++)
		{
			List<Vec4i> dataToSend = new ArrayList<>();

			for (int j = 0; j < count; j++)
			{
				int index = i * count + j;
				if (index < currentData.size())
				{
					dataToSend.add(currentData.get(index));
				}
				else
				{
					finished = true;
					break;
				}
			}
			server.tcpSend(new SyncBlocksPacket(dataToSend), address, port);
			if (finished)
				break;
		}

		for (int i = 0; i < server.getCore().getGame().getEntityManager().getNetworkableEntites().size(); i++)
		{
			int id = server.getCore().getGame().getEntityManager().getNetworkableEntites().get(i);
			if (id == this.id) 
				continue;
			Entity e = server.getCore().getGame().getEntityManager().getEntities().get(id);
			if (e instanceof Player)
				server.tcpSend(new EntitySyncPacket((Player) e), address, port);
		}

		/* Game Mode managment */
		GameMode mode = server.getCore().getGame().getGameMode();
		mode.onPlayerConnect(id, server);
//		GameCore.getInstance().getGame().getGameMode().onPlayerSpawn(id, server);
//		this.position = GameCore.getInstance().getGame().getGameMode().getPlayerSpawn(id);
//		server.tcpSend(new RespawnPacket((Player) GameCore.getInstance().getGame().getEntityManager().get(id), this.position), address, port);

		if (!version.equals(GameCore.GAME_SUB_VERSION))
		{
			server.log(name + " tried to connect with an invalid version: v" + version + "  Current: v" + GameCore.GAME_SUB_VERSION);
			server.tcpSendToAll(new KickPacket(id, "Invalid game version, please download the latest one: ubercube.github.io"));
			GameCore.getInstance().getGame().remove(id);
//			server.getTcp().disconnectClient(address, port);
			return;
		}
	}

	public void process(NetworkableClient client, InetAddress address, int port)
	{
		if (client.getCore().getGame().getPlayer().getID() != id)
		{
			NetworkedPlayer player = new NetworkedPlayer(id, name, position, rotation, address.getHostName(), port);
			player.setDead(true);
			client.getCore().getGame().spawn(player);
			client.log(name + " just connected !");
		}
		else
		{
			client.log("You just connected as " + name + " with id: " + id);
			client.getCore().getGame().createWorld(seed);
			client.setConnected(true);
		}
		client.console(name + " just connected !");
	}
}