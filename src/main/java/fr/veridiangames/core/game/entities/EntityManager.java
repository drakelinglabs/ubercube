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

package fr.veridiangames.core.game.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import fr.veridiangames.client.Ubercube;
import fr.veridiangames.core.GameCore;
import fr.veridiangames.core.game.entities.audio.AudioSource;
import fr.veridiangames.core.game.entities.bullets.Bullet;
import fr.veridiangames.core.game.entities.components.ECRender;
import fr.veridiangames.core.game.entities.components.EComponent;
import fr.veridiangames.core.game.entities.particles.ParticleSystem;
import fr.veridiangames.core.game.entities.player.ClientPlayer;
import fr.veridiangames.core.game.entities.player.NetworkedPlayer;
import fr.veridiangames.core.game.entities.player.Player;
import fr.veridiangames.core.maths.Vec3;

/**
 * Created by Marccspro on 3 f�vr. 2016.
 */
public class EntityManager
{
	private List<Integer>						keys;
	private ConcurrentHashMap<Integer, Entity>	entities;
	private List<Integer>						renderableEntities;
	private List<Integer>						networkableEntities;
	private List<Integer>						playerEntities;
	private List<Integer>						particleEntities;
	private List<Integer>						audioEntities;

	public EntityManager()
	{
		keys = new ArrayList<>();
		entities = new ConcurrentHashMap<>();
		renderableEntities = new ArrayList<>();
		networkableEntities = new ArrayList<>();
		playerEntities = new ArrayList<>();
		particleEntities = new ArrayList<>();
		audioEntities = new ArrayList<>();
	}

	public void update(GameCore core)
	{
		for (int i = 0; i < keys.size(); i++)
		{
			Entity e = get(keys.get(i));
			if (e == null)
			{
				remove(keys.get(i));
				continue;
			}
			e.update(core);
			
			if (e.isDestroyed())
				remove(keys.get(i));
		}
	}

	public void add(Entity e)
	{
		if (e.contains(EComponent.RENDER))
			renderableEntities.add(e.getID());
		
		if (e.contains(EComponent.NETWORK))
			networkableEntities.add(e.getID());

		if (e instanceof Player)
			playerEntities.add(e.getID());

		if (e instanceof ParticleSystem)
			particleEntities.add(e.getID());

		if (e instanceof AudioSource)
			audioEntities.add(e.getID());

		keys.add(e.getID());
		entities.put(e.getID(), e);
	}
	
	public void remove(int id)
	{
		if (renderableEntities.contains(id))
			renderableEntities.remove((Integer) id);

		if (networkableEntities.contains(id))
			networkableEntities.remove((Integer) id);

		if (playerEntities.contains(id))
		{
			Entity e = entities.get(id);
			if (e instanceof NetworkedPlayer)
			{
				NetworkedPlayer p = (NetworkedPlayer) e;
				GameCore.getInstance().getGame().getPhysics().removeBody(p.getBody());
			}
			playerEntities.remove((Integer) id);
		}

		if (particleEntities.contains(id))
			particleEntities.remove((Integer) id);

		if (audioEntities.contains(id))
			audioEntities.remove((Integer) id);

		entities.remove(id);
		keys.remove((Integer) id);
	}
	
	public Entity get(int id)
	{
		return entities.get(id);
	}

	public Map<Integer, Entity> getEntities()
	{
		return entities;
	}

	public List<Entity> getEntitiesInRange(Vec3 pos, float range)
	{
		List<Entity> result = new ArrayList<Entity>();
		for (Entry<Integer, Entity> entry : entities.entrySet())
		{
			Entity e = entry.getValue();
			if (e.contains(EComponent.RENDER))
			{
				Vec3 epos = ((ECRender) e.get(EComponent.RENDER)).getTransform().getPosition();
				float xx = epos.x - pos.x;
				float yy = epos.y - pos.y;
				float zz = epos.z - pos.z;
				float sqrt = xx * xx + yy * yy + zz * zz;
				
				if (sqrt < range * range)
					result.add(e);
			}
		}
		
		return result;
	}

	public List<Entity> getEntitiesAt(Vec3 point)
	{
		List<Entity> result = new ArrayList<>();
		for (int i = 0; i < entities.size(); i++)
		{
			Entity e = entities.get(keys.get(i));
			if (e.contains(EComponent.RENDER))
			{
				Vec3 epos = ((ECRender) e.get(EComponent.RENDER)).getTransform().getPosition();
				Vec3 esize = ((ECRender) e.get(EComponent.RENDER)).getScale();

				if (point.x > epos.x - esize.x && point.x < epos.x + esize.x &&
						point.y > epos.y - esize.y && point.y < epos.y + esize.y &&
						point.z > epos.z - esize.z && point.z < epos.z + esize.z)
				{
					result.add(e);
					break;
				}
			}
		}
		return result;
	}

	// TODO: remove completely this fucked up function
	public Entity getEntityAt(Vec3 point, String... targetTags)
	{
		Entity result = null;
		try
		{
			for (int i = 0; i < entities.size(); i++)
			{
				Entity e = entities.get(keys.get(i));
				boolean jumpIteration = false;
				for (int j = 0; j < targetTags.length; j++)
				{
					String target = targetTags[j];
					if (!e.getTags().contains(target))
					{
						jumpIteration = true;
					}
					else
					{
						jumpIteration = false;
						continue;
					}
				}
				if (jumpIteration)
					continue;

				if (e.contains(EComponent.RENDER))
				{
					Vec3 epos = ((ECRender) e.get(EComponent.RENDER)).getTransform().getPosition();
					Vec3 esize = ((ECRender) e.get(EComponent.RENDER)).getScale();

					if (point.x > epos.x - esize.x && point.x < epos.x + esize.x &&
						point.y > epos.y - esize.y && point.y < epos.y + esize.y &&
						point.z > epos.z - esize.z && point.z < epos.z + esize.z)
					{
						result = e;
						break;
					}
				}
			}
		}
		catch (Exception e)
		{}
		return result;
	}

	public List<Integer> getKeys()
	{
		return keys;
	}

	public List<Integer> getRenderableEntites()
	{
		return renderableEntities;
	}
	
	public List<Integer> getNetworkableEntites()
	{
		return networkableEntities;
	}

	public List<Integer> getPlayerEntites()
	{
		return playerEntities;
	}

	public List<Integer> getParticleEntities()
	{
		return particleEntities;
	}

	public List<Integer> getAudioEntities()
	{
		return audioEntities;
	}
}
