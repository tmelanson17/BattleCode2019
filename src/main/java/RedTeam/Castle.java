/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RedTeam;

import BCRobot.Nav;
import java.awt.Point;

//Imports from BCRObot base classes
import BCRobot.Robot;
import BCRobot.Specs;

/**
 *
 * @author Kieran
 */
public class Castle extends Robot{
    public void takeTurn()
    {
       // System.out.println("Castle Goin'");
        
        
       //Build a Pilgrim, Prophet, Preacher in order 
       //Use the modulo (%) operator to cycle through each
        
       int build_unit = 0;     //Unit type to build
       int build_fuel = 0;     //Fuel required
       int build_karb = 0;     //Karb required
       Point build_pos = new Point (0,-1); //Position (above castle)
       
       //Determine which unit
       if (turn % 3 == 0) build_unit = Specs.unitPilgrim;
       if (turn % 3 == 1) build_unit = Specs.unitProphet;
       if (turn % 3 == 2) build_unit = Specs.unitPreacher;
       
       //Determine cost to build desired unit
       build_fuel = Specs.getFuelBuildCost(build_unit);
       build_karb = Specs.getKarbBuildCost(build_unit);
       
       //Now run through a set of checks if okay to build
       // - Must be passable
       // - Must have enough fuel and karbonite
       // Need to convert build position (relative) to absolute pos)
       Point pos = new Point (build_pos.x + x, build_pos.y+y);
       if (!Nav.isPassable(pos, map, robot_map))  return;
       if (build_fuel > fuel || build_karb > karbonite) return;
       
       //So far so good, do the build
       //Build method position is relative 
       buildUnit (build_unit, build_pos.x, build_pos.y);
    }
}

