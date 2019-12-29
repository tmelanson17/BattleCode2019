/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RedTeam;

import java.awt.Point;


import BCRobot.Nav;
import BCRobot.Robot;
import BCRobot.Specs;

/**
 *
 * @author Kieran
 */
public class Pilgrim  extends Robot{
    public void takeTurn()
    {
       // System.out.println("Pilgrim Goin'");
        
        //Move up 1 square (if can)
        Point move_pos = new Point (0,-1); //Position (above castle)
      
        //Determine cost to build desired unit
        //Cost is based on distance squared
        int dist      = (move_pos.x*move_pos.x + move_pos.y * move_pos.y);
        int move_fuel = dist * Specs.getMoveFuel(unit);
       
       //Now run through a set of checks if okay to move
       // - Must be passable
       // - Must have enough fuel
       // Need to convert build position (relative) to absolute pos)
       Point pos = new Point (move_pos.x + x, move_pos.y+y);
       if (!Nav.isPassable(pos, map, robot_map))  return;
       if (move_fuel > fuel)                      return;
       
       //So far so good, do the move
       //Move method position is relative 
       move (move_pos.x, move_pos.y);

    }
}
