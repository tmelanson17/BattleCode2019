/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BCRobot;

import java.awt.Point;
import java.util.ArrayList;

//Robot is a base class, all custom robots (Castle, Pilgrim, etc) 
//Must extend this base class. 

public class Robot 
{

    //Variables that do not change 
    public boolean map           [][];  //Locations of passable (true) areas
    public boolean karbonite_map [][];  //True = resource present 
    public boolean fuel_map      [][];  //True = fuel present
    public boolean robot_map     [][];  //True = robot present
    public int field_size;              //Dimensions of field 
    public int id;                      //ID of this robot
    public int unit;                    //Type of robot 
    public int castle_talk = 0;         //Castle Talk value
    
    
    public int karbonite;               //Amount of team carbonite
    public int fuel;                    //Amount of team fuel
    public ArrayList <Robot> visible_robots = new ArrayList <>();  //All visible robots
    
    //Variables for me
    public int x;                       //Robots x,y position
    public int y;
    public int health;
    public int team;
    public int carry_fuel       = 0;       //Fuel currently carrying
    public int carry_karbonite  = 0;
    public int turn   = 0;       //1-Based 
    public Action action  = new Action ();
    public int pendingMessage = 0;
    public ArrayList <Integer> enemyCastles = new ArrayList <>();
    
    
//True if within field boundaries
public boolean inRange (int x, int y) 
{
    if (x < 0 || x >= field_size)  return false;
    if (y < 0 || y >= field_size)  return false;
    return true;
}        

//You must override this in your custom class and choose from 
//below options
public void takeTurn ()
{
    System.out.println ("Robot taking turn!");
}

//Returns TRUE if value at x,y is a neighbor (adjacent)
//adj_x,adj_y are absolute (not delta) values
public boolean isNeighbor (Point pos)
{
    int dx = x - pos.x;
    int dy = y - pos.y;
    return ((dx*dx + dy*dy) <= 2);

}
//Returns Robot if this unit is a neighbor (adjacent)
public Robot findNeighbor (int unit_type)
{   
    for (int i = 0; i<visible_robots.size(); i++)
        {
        Robot r = visible_robots.get(i);
        if (r.team != team)           continue;
        if (r.unit != unit_type)      continue;
        if (!isNeighbor (r.getPos())) continue;  
        return r;
        } 
    return null;
}
//Returns Robot at the supplier position
public Robot findRobot (Point pos)
{   
    for (int i = 0; i<visible_robots.size(); i++)
        {
        Robot r = visible_robots.get(i);
        if (r.team != team)          continue;
        if (r.x != pos.x)            continue;
        if (r.y != pos.y)            continue;  
        
        //System.out.printf ("Robot (%d) at (%d,%d)\n",r.id, r.x,r.y);
        return r;
        } 
    return null;
}


//Find a robot that can recieve all the mined resources
public Robot findGiveRobot ()
{   
    for (int i = 0; i<visible_robots.size(); i++)
        {
        Robot r = visible_robots.get(i);
        if (r.team != team)           continue;
        if (!isNeighbor (r.getPos())) continue;  
        
        //Castle can take all
        if (r.unit == Specs.unitCastle) return r;
        
        //Now check if the robot has the capacity to receive
        if (r.carry_fuel + carry_fuel > Specs.getFuelCapacity(r.unit)) continue;
        if (r.carry_karbonite + carry_karbonite > Specs.getKarbCapacity(r.unit)) continue;
        return r;
        } 
    return null;
}

//Set castle_talk parameter
public void castleTalk  (int message)
{
    castle_talk = message;
}

public void move (int dx, int dy)
{
    action.command = Specs.commandMove;   //ID of move to make
    action.dx = dx;
    action.dy = dy;
}

public void mine ()
{
    //Requirements
    //- Must be on resource
    //- Must have enough capacity
    action.command = Specs.commandMine;  
    action.dx      = 0;
    action.dy      = 0;
}
public void pass ()
{
    //Requirements
    //- Must be on resource
    //- Must have enough capacity
    action.command = Specs.commandPass;  
}


public void give (int dx, int dy, int karb, int fuel)
{
    //Requirements
    //- Must be adjacent squares
    //- Receiver must have enough capacity
    //- Must be a receiver on the square
    action.command = Specs.commandGive;  
    action.dx      = dx;
    action.dy      = dy;
    action.karb    = karb;
    action.fuel    = fuel;
}
//Returns TRUE if at the indicated position
public boolean atPosition (Point pos)
{
    return (pos.x == x && pos.y == y);
}

public void attack (int dx, int dy)
{
    action.command = Specs.commandAttack;  
    action.dx = dx;
    action.dy = dy;
}

public void buildUnit (int unit_type, int dx, int dy)
{
    action.command = Specs.commandBuild;  
    action.dx      = dx;
    action.dy      = dy;
    action.unit    = unit_type;
}

public void log (String message)
{
    System.out.println(message);
}

public ArrayList <Robot> getVisibleRobots ()
{
    return visible_robots;
}

public Point  getPos ()
{
    return new Point(x,y);
}

public void print ()
{        
    System.out.printf("%s ", Specs.getTeamName (team));
    System.out.printf("%s ", Specs.getUnitName (unit));
    System.out.printf("(%d):", id);
    System.out.printf("(%d,%d) ", x, y);
    System.out.printf("Health: %d) ", health);
    System.out.printf("\n");
}
//Returns true if Robot r is within range to be attacked
public boolean inAttackRange (Robot r)
{
    int dist =  Nav.sqDist (getPos(), r.getPos());
    if (dist < Specs.getAttackMin (unit)) return false;
    if (dist > Specs.getAttackMin (unit)) return false;
    return true;
}

//robots MUST be a list of visible robots
public ArrayList<Robot> getAttackableRobots ()
{
        ArrayList<Robot> result  = new ArrayList<>();
        //System.out.printf ("\nMy Position: (%d,%d)\n", x,y);
        //System.out.printf ("Attackable Enemy Robots: \n");
        for (int i=0; i<visible_robots.size(); i++)
        {
            Robot r  = visible_robots.get(i);
            int dist = Nav.sqDist(getPos(),r.getPos());
            if (r.team == team) continue; 
            if (dist < Specs.getAttackMin(unit)) continue;
            if (dist > Specs.getAttackMax(unit)) continue;
            //r.print();
            result.add (r);
        }
        return result;
}

//robots MUST be a list of visible robots
//These robots are too close
public ArrayList<Robot> getAttackingRobots ()
{
        ArrayList<Robot> result  = new ArrayList<>();
        for (int i=0; i<visible_robots.size(); i++)
        {
            Robot r  = visible_robots.get(i);
            int dist = Nav.sqDist(getPos(),r.getPos());
            if (r.team == team) continue; 
            if (dist < Specs.getAttackMin(unit)) result.add (r);
        }
        return result;
}


public Robot getAttackTarget  (ArrayList <Robot> targets)
{
    Robot r;
    for (int i=0; i<targets.size(); i++)
        {
        r = targets.get(i);
        if (r.team == team)     {continue;}
        if (!inAttackRange (r)) {continue;}
        //Not on my team, and in range = Fire away!
        return r;
        }
    return null; //Nothing to shoot at!
}


public boolean [][] getKarboniteMap ()
{
    return karbonite_map;
}

public boolean [][] getFuelMap ()
{
    return fuel_map;
}

//Returns TRUE if currently sitting on a resource
public boolean onResource ()
{
    if (fuel_map[y][x]) return true;
    if (karbonite_map[y][x]) return true;
    return false;
}

}