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

package fr.veridiangames.core;

import fr.veridiangames.core.game.Game;
import fr.veridiangames.core.utils.FileUtils;

/**
 * Created by Marccspro on 29 janv. 2016.
 */
public class GameCore
{
	public static final String GAME_NAME = "Ubercube";
	public static final String GAME_VERSION_NAME = "Pre-Alpha 1.1";
	public static final String GAME_SUB_VERSION = FileUtils.getGitCommitsCount();
	public static final String RESOURCES_PATH = "";

	private static boolean displayNetworkDebug = false;

	private static GameCore instance;

	private boolean ignoreAction;

	public double deltaTime;
	
	private Game mainGame;
	
	public GameCore()
	{
		instance = this;
		mainGame = new Game(this);

		ignoreAction = false;
	}
	
	public void update()
	{
		mainGame.update();
	}

	public void updatePhysics()
	{
		mainGame.updatePhysics();
	}
	
	public Game getGame()
	{
		return mainGame;
	}
	
	public void setDeltaTime(double delta)
	{
		this.deltaTime = delta;
	}

	public static GameCore getInstance()
	{
		return instance;
	}

	public static boolean isDisplayNetworkDebug()
	{
		return displayNetworkDebug;
	}

	public static void setDisplayNetworkDebug(boolean displayNetworkDebug)
	{
		GameCore.displayNetworkDebug = displayNetworkDebug;
	}

	public boolean isIgnoreAction()
	{
		return ignoreAction;
	}

	public void setIgnoreAction(boolean ignoreAction)
	{
		this.ignoreAction = ignoreAction;
	}
}