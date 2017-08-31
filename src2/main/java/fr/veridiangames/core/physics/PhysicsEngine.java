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

package fr.veridiangames.core.physics;

import java.util.ArrayList;
import java.util.List;

import fr.veridiangames.core.GameCore;

/**
 * Created by Marccspro on 22 janv. 2016.
 */
public class PhysicsEngine
{
	private List<Rigidbody> bodys;

	public PhysicsEngine()
	{
		this.bodys = new ArrayList<>();
	}

	public void update(GameCore core, int precision)
	{
		float delta = 1.0f / precision;
		for (int p = 1; p <= precision; p++)
			for (int i = 0; i < this.bodys.size(); i++)
			{
				Rigidbody a = this.bodys.get(i);
				a.applyGravity(delta);
				a.applyForces(delta);
				a.updateDragFactor(delta);
				a.updateVelocity(delta);
				a.updateDrag(delta);
				if (!a.isIgnoreOthers())
					for (int j = 0; j < this.bodys.size(); j++)
					{
						Rigidbody b = this.bodys.get(j);
						if (a == b)
							continue;

						if (b.isIgnoreOthers())
							continue;

						CollisionData data = a.getCollisionData(b);
						if (data.isCollision())
							a.handleCollision(data, b.getCollider());
					}
				a.handleWorldCollision(core.getGame().getWorld(), delta);
				a.updatePosition();
			}
	}

	public void addBody(Rigidbody body)
	{
		this.bodys.add(body);
	}

	public void removeBody(Rigidbody body)
	{
		this.bodys.remove(body);
	}
}
