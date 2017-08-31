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

import fr.veridiangames.core.game.entities.particles.ParticleSystem;
import fr.veridiangames.core.network.NetworkableClient;
import fr.veridiangames.core.network.NetworkableServer;
import fr.veridiangames.core.utils.DataBuffer;

/**
 * Created by Tybau on 10/06/2016.
 */
public class ParticlesRemovePacket extends Packet
{
    private int     id;

    public ParticlesRemovePacket()
    {
        super(PARTICLES_REMOVE);
    }

    public ParticlesRemovePacket(ParticleSystem system)
    {
        super(PARTICLES_REMOVE);
        this.data.put(system.getID());

        this.data.flip();
    }

    public ParticlesRemovePacket(ParticlesRemovePacket packet)
    {
        super(PARTICLES_REMOVE);
        this.data.put(packet.id);

        this.data.flip();
    }

    @Override
	public void read(DataBuffer buffer)
    {
        this.id = buffer.getInt();
    }

    @Override
	public void process(NetworkableServer server, InetAddress address, int port)
    {
        server.tcpSendToAll(new ParticlesRemovePacket(this));
    }

    @Override
	public void process(NetworkableClient client, InetAddress address, int port)
    {
        client.getCore().getGame().getEntityManager().remove(this.id);
    }
}
