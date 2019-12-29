/*
 * Add this file to your project.  It should be the only module with
   a 'main' program below

   If successful, your program should compile, and when run, display:


*/
package battlecode2019;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

//Imports specific to this project
import BCRobot.Robot;
import BCRobot.Specs;
import BCRobot.Nav;

import java.awt.Point;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;



public class BattleCode2019 {

    private static boolean map[][];    //true = area is passable (no blockage)
    private static boolean fuel[][];   //true = fuel at this location
    private static boolean karb[][];   //true = karb at this location
    private static int map_size;       //Map Size NxN map_size = N
    private static ArrayList<Robot> robots;  //Array of all robots
    private static int team_fuel[] = {500,500};           
    private static int team_karb[] = {100,100};
    private static int robot_count = 0;  //Robot ID counter (not same as # robots)
    private static int turn        = 0;  //Number of turns
    private static int turn_max    = 1000;  //Stop at this number of turns
    
    private static OutputStream replay = null;
    private static int seed            = 1773232113;
    private static boolean fVerbose    = false;
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    
    {
        
    //Load field and Castle positions from JSON files
    if (!loadField  ()) return;
    if (!loadRobots ()) return;
    
    //Open path to replay stream
    String folder = System.getProperty("user.dir");
    //For testing, set extension to out to compare to actual replays
    String file   = String.format("%d.bc19", seed);
    Path f = Paths.get(folder, file);
    System.out.printf ("Saving replay:\n%s\n",f.toString());
    try {
        replay = new FileOutputStream (f.toString());
        } catch (FileNotFoundException ex) 
            {
            System.out.printf("Failed to create replay file!\n");
            return;
            }
    
    //Iterate through turns
    //If takeTurn returns different robot, add it to queue
    //Stop when all castles destroyed
    int index  = 0;
    turn  = 1;
    int round = 0;
    fVerbose = false;

    int redCastle  = 0;  //Number of red castles (calc at end of each turn)
    int blueCastle = 0;  //Number of blue castles
    
    while (robots.size() > 0)
    {
        //if (++round > 100) break;
        Robot r = robots.get(index);
        //If first turn, add 25 fuel to each team
        if (index == 0)
            {
            ++round;
            adjTeamFuel (Specs.teamRed,  25);
            adjTeamFuel (Specs.teamBlue, 25);
            }
        populate (r);  //Populate with settings before turn
        
        //Turn | Round | Robot
        //System.out.printf ("(%4d|%3d|%2d)",  turn, round, index+1);
        //loadReplay (r, turn);
        //Override takeTurn and use sample fiel
        r.takeTurn ();
        
        if (fVerbose)
        {
            System.out.printf("%s ", Specs.getTeamName (r.team));
            System.out.printf("%s ", Specs.getUnitName (r.unit));
            System.out.printf("(%d,%d):", r.x,r.y);
            System.out.printf("%s (", Specs.getCommandName(r.action.command));
            System.out.printf("%d,%d) ", r.action.dx, r.action.dy);
            System.out.printf("Karb: %d, Fuel: %d) ", r.action.karb, r.action.fuel);
            System.out.printf("\n");
        }
        
        //Turn is now populated with parms
        //Attack is handled here
        if (r.action.command == Specs.commandAttack)
        {
            if (!processAttack (r)) r.action.command = Specs.commandPass;
        }    
        
        //If build, that is handle
        if (r.action.command == Specs.commandBuild)
        {
            if (!processBuild (r))  r.action.command = Specs.commandPass;
        }    

        //If Move, processMove
        if (r.action.command == Specs.commandMove)
        {
            if (!processMove (r))  r.action.command = Specs.commandPass;
        }    

        //If Mine, processMove
        if (r.action.command == Specs.commandMine)
        {
            if (!processMine (r))  r.action.command = Specs.commandPass;
        }    
        
        //If Give, processMove
        if (r.action.command == Specs.commandGive)
        {
            if (!processGive (r))  r.action.command = Specs.commandPass;
        }    
        
        
        if (r.action.command == Specs.commandPass)
        {
            processPass (r);
        }    
        
        //Find position of r in index and add 1
        //Some deleted entries may change position
        index = robots.indexOf(r);
        if (++index >= robots.size()) index=0;
        if (++turn > turn_max) break; //Stop after 1000 turns
        
        //If no castles on a team game over
        blueCastle = 0;
        redCastle  = 0;
        for (int i=0; i<robots.size();i++)
            {
            if (robots.get(i).unit == Specs.unitCastle)
                {
                if (robots.get(i).team == 0) ++redCastle;
                if (robots.get(i).team == 1) ++blueCastle;
                }
            }
        if (redCastle == 0)  break;
        if (blueCastle == 0) break;
    }    
    
    
    System.out.println("Game Over!");
    System.out.printf ("Red Castles: %d\n", redCastle);
    System.out.printf ("Red Fuel: %d\n", getTeamFuel(0));
    System.out.printf ("Red Karb: %d\n", getTeamKarb(0));

    System.out.printf ("Blue Castles: %d\n", blueCastle);
    System.out.printf ("Blue Fuel: %d\n", getTeamFuel(1));
    System.out.printf ("Blue Karb: %d\n", getTeamKarb(1));
    }

   
//Load field via JSON into local specs
public static boolean loadField ()
    {
        String folder = System.getProperty("user.dir");
        String file   = String.format("Maps\\%d_field.json", seed);
        Path f = Paths.get(folder, file);
        String src;
        System.out.printf("Loading Field: \n%s\n",f.toString());
        try {
            src = new String (Files.readAllBytes (f) );
            try {
                JSONObject jsonObject = new JSONObject(src);
                loadMap (jsonObject);
                } catch (JSONException ex) 
                    {
                    Logger.getLogger(BattleCode2019.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                    }
            } catch (IOException ex) 
                 {
                 System.out.printf("Failed to load field!  Check Location above\n");
                 return false;
                 }
    return true;
    }

//Load move from replay file
public static void loadReplay (Robot r, int count) 
    {
        String folder = System.getProperty("user.dir");
        String file   = String.format("Replays\\%d.bc19", seed);
        Path f = Paths.get(folder, file);
        int pos = 6 + 8 * count - 4; 
        InputStream is;
        try {
            is = new FileInputStream (f.toString());
            byte move[] = {0,0,0,0};
            try {
                is.skip(pos);
                is.read(move);
                loadMove (r, move);
                //Parse the move into r.action
                is.close ();
                } catch (IOException ex) {}
        
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BattleCode2019.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

public static void loadMove (Robot r, byte [] move)
    {    
        //Now characterize
        // Build = 11 xxx xxx = 3
        int command = move[0];    //Start with low value
        command  *= 4;            //Move to high 3 byte
        command += (move[1] & 0xC0) >> 6;
        /*
        System.out.printf ("%02X|",  move[0]);
        System.out.printf ("%02X|",  move[1]);
        System.out.printf ("%02X|",  move[2]);
        System.out.printf ("%02X:",  move[3]);
        System.out.printf ("%d->",   command);
        */
        
        int dx = (move[2] & 0xFF ); 
        int dy = (move[3] & 0xFF); 
        if (dx >= 128) dx = 128 - dx;
        if (dy >= 128) dy = 128 - dy;
        //Default value;
        r.pass();
        
        if (command == 1) {r.move(dx,dy);}
        if (command == 2) {r.attack(dx,dy);}
        
        // Build = 11 xxx xxx = 3
        if (command == 3) 
            {
            // Mask 00111000 = 0x38
            // Mask 00000111 = 0x07   
            dx = (move[1] & 0x38) >> 3; 
            dy = (move[1] & 0x07); 
            if (dx >= 4) dx = 4 - dx;
            if (dy >= 4) dy = 4 - dy;
            int unit = move[2];
            r.buildUnit (unit,dx,dy);
            }

        if (command == 4) {r.mine();}
        if (command == 6) 
            {
            // Mask 00111000 = 0x38
            // Mask 00000111 = 0x07   
            dx = (move[1] & 0x38) >> 3; 
            dy = (move[1] & 0x07); 
            if (dx >= 4) dx = 4 - dx;
            if (dy >= 4) dy = 4 - dy;
            int action_karb = move[2];
            int action_fuel = move[3];
            r.give (dx,dy,action_karb, action_fuel);
            }
    }


//Load robots via JSON into local specs
public static boolean loadRobots ()
    {
        String folder = System.getProperty("user.dir");
        String file   = String.format("Maps\\%d_robot.json", seed);
        Path f = Paths.get(folder, file);
        String src;
        System.out.printf("Loading Castles: \n%s\n",f.toString());
        robots = new ArrayList<> ();
        
        try {
            src = new String (Files.readAllBytes (f) );
            try {
                JSONObject jsonObject = new JSONObject(src);
                loadRobot (jsonObject);
                } catch (JSONException ex) 
                    {
                    Logger.getLogger(BattleCode2019.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                    }
        } catch (IOException ex) {
            System.out.printf("Failed to load Castle positions!  Must be in location above\n");
            return false;    
        }
    
    return true;
    }

public static void loadRobot (JSONObject jsonObject)
{
    try {
        JSONArray  blocks = jsonObject.getJSONArray("robots");
        int num_blocks = blocks.length();
        for (int i=0;i<num_blocks;i++)
        {
            try 
            {
                JSONObject robot = blocks.getJSONObject(i);
                Robot newR;
                int team = robot.getInt("team");
                int x    = robot.getInt("x");
                int y    = robot.getInt("y");
                if (team == 0) newR  = new RedTeam.Castle ();
                else newR  = new BlueTeam.Castle ();
                initRobot (newR, ++robot_count, team, Specs.unitCastle, x, y);
                robots.add(newR);
            } catch (JSONException ex) {
            Logger.getLogger(BattleCode2019.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        
        } catch (JSONException ex) {
            Logger.getLogger(BattleCode2019.class.getName()).log(Level.SEVERE, null, ex);
        }
        


}    
//--------------------------------------
//Load field map
//--------------------------------------
public static void loadMap (JSONObject jsonObject)
{
        try {
            map_size = ((Number)jsonObject.get("size")).intValue();
            map  = new boolean [map_size][map_size];
            fuel = new boolean [map_size][map_size];
            karb = new boolean [map_size][map_size];
            //Populate with true (passable)
            for (int x=0;x<map_size;x++)
                for (int y=0;y<map_size;y++)
                {
                    map  [y][x] = true;
                    fuel [y][x] = false;
                    karb [y][x] = false;
                }
            //Populate blockages
            JSONArray blocks = jsonObject.getJSONArray("impassable");
            int num_blocks = blocks.length();
            for (int i=0;i<num_blocks;i++)
                {
                JSONObject pos = blocks.getJSONObject(i);
                int x = pos.getInt("x");
                int y = pos.getInt("y");
                map[y][x] = false;
                //System.out.printf("(%d,",x);
                //System.out.printf("%d) = ",y);
                //System.out.printf("%b%n",map[y][x]);
                }

            //Print out values
            /*
            for (int x=0;x<map_size;x++)
                for (int y=0;y<map_size;y++)
                {
                    System.out.printf("(%d,",x);
                    System.out.printf("%d) = ",y);
                    System.out.printf("%b%n",map[y][x]);
                }
            */
            
            //Populate fuel locations
            JSONArray jsonFuel = jsonObject.getJSONArray("fuel");
            int num_fuel = jsonFuel.length();
            for (int i=0;i<num_fuel;i++)
                {
                JSONObject pos = jsonFuel.getJSONObject(i);
                int x = pos.getInt("x");
                int y = pos.getInt("y");
                fuel[y][x] = true;
                }

            //Populate karb locations
            JSONArray jsonKarb = jsonObject.getJSONArray("karbonite");
            int num_karb = jsonKarb.length();
            for (int i=0;i<num_karb;i++)
                {
                JSONObject pos = jsonKarb.getJSONObject(i);
                int x = pos.getInt("x");
                int y = pos.getInt("y");
                karb[y][x] = true;
                }
            
            
        } catch (JSONException ex) {
            Logger.getLogger(BattleCode2019.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    
}

public static boolean processAttack (Robot r)
{
    //Requirements
    // - Must be within range
    // - Must have enough fuel
    // - Robot must be on square
    //System.out.printf("Attacker: ");
    //r.print();

    
    int dx = r.action.dx;
    int dy = r.action.dy;
    int x  = r.x + dx;
    int y  = r.y + dy;
    
    int cost_fuel  = Specs.getAttackFuel (r.unit);
    int avail_fuel = getTeamFuel (r.team);
    int dist       = dx * dx + dy * dy;
    int attack_min = Specs.getAttackMin (r.unit);
    int attack_max = Specs.getAttackMax (r.unit);
    
    if (dist < attack_min)           return false;
    if (dist > attack_max)           return false;
    if (cost_fuel > avail_fuel)      return false;
    
    Robot target = findRobot (x,y); 
    if (target == null)      return false;
    
    target.health -= Specs.getAttackDamage (r.unit);
    if (target.health <= 0) 
        {
        System.out.printf("Killed: ");
        target.print ();
        robots.remove (target);  //Remove it from robot list
        }
    //Adjust fuel
    adjTeamFuel (r.team, -cost_fuel);
    
    //Replace Log entry
    if (dx < 0) dx = 128 - dx;
    if (dy < 0) dy = 128 - dy;
    byte turn [] = {0,0,0,0};
    turn[1] = (byte)0x80;
    turn[2] = (byte)dx;
    turn[3] = (byte)dy;
    logTurn (turn);
    return true;
}


//Robot r is moving
public static boolean processMove (Robot r)
{
    int dx = r.action.dx;
    int dy = r.action.dy;
    Point pos = new Point (r.x + dx, r.y + dy);
    //Requirements
    //- Must be on field and within robots range
    //- Must be not occupied
    //- Must be passable
    //- Must be a move within robots speed
    int dist     = dx*dx + dy*dy;
    int max_dist = Specs.getSpeed(r.unit);
    
    //If dist = 0; treat as pass
    if (dist == 0)  return false;
    
    if (dist > max_dist)  
        {
        System.out.printf ("Move Failed: Too far (%d, max %d)!\n", dist,max_dist);
        return false;           
        }

    if (!Nav.isPassable(pos, map, r.robot_map)) 
        {
        System.out.printf ("Move Failed: Not passable (%d,%d)->(%d,%d)!\n", r.x,r.y,pos.x,pos.y);
        return false;
        }    

    int avail_fuel = getTeamFuel(r.team);
    int move_fuel  = Specs.getMoveFuel(r.unit) * dist;
    if (move_fuel > avail_fuel)    
    {
        System.out.printf ("Move Failed: Not enough fuel!\n");
        return false;
    }
    //All looks good so far - do the move
    r.x = pos.x;
    r.y = pos.y;
    adjTeamFuel (r.team, -move_fuel);
    
    
    //Replace Log entry
    byte turn [] = {0,0,0,0};
    if (dx < 0) dx = 128 - dx;
    if (dy < 0) dy = 128 - dy;
    turn[1] = new Integer (0x40).byteValue();
    turn[2] = (byte) dx;
    turn[3] = (byte) dy;
    logTurn (turn);
    return true;
}

//Process Mine Request
public static boolean processMine (Robot r)
{
    
    //Req'ts
    //- Must be on resource
    //- Must have capacity
    int x = r.x;
    int y = r.y;
    
    //Test for fuel
    int resource = 0;
    if (fuel[y][x])
    {
        resource = 10;
        if (r.carry_fuel + resource > Specs.getFuelCapacity(r.unit)) return false;
        r.carry_fuel += resource;
    }

    if (karb[y][x])
    {
        resource = 2;
        if (r.carry_karbonite + resource > Specs.getKarbCapacity(r.unit)) return false;
        r.carry_karbonite += 2;
    }

    adjTeamFuel(r.team,-1);
    //Replace Log entry
    byte turn [] = {1,0,0,0};
    logTurn (turn);
    return true;
}

//Process a Pass (no move)
public static boolean processPass (Robot r)
{
    //Replace Log entry
    byte turn [] = {0,0,0,0};
    logTurn (turn);
    return true;
}


//Robot r is requesting build
public static boolean processBuild (Robot r)
{
    int unit = r.action.unit;
    int dx   = r.action.dx;
    int dy   = r.action.dy;
    int dist = dx * dx + dy * dy;
    int cost_fuel  = Specs.getFuelBuildCost (unit);
    int cost_karb  = Specs.getKarbBuildCost (unit);
    int avail_fuel = getTeamFuel (r.team);
    int avail_karb = getTeamKarb (r.team);
    Point pos = new Point (r.x + dx, r.y + dy);
    
    //Run checks 
    // Must be adjacent
    if (dist > 2)  
        {
        System.out.printf ("Build failed: non-adjacent cell (%d,%d)\n",dx,dy);
        return false;  //Not adjacent
        }
    
    // Must be a passable square (no blockages or other robots)
    if (!Nav.isPassable (pos, map, r.robot_map))
    {
        System.out.printf ("Build failed: not a passable cell (%d,%d)\n",pos.x,pos.y);
        return false;
    }
    
    // Not enough fuel
    if (cost_fuel > avail_fuel) 
        {
        System.out.printf ("Build failed: not enough fuel (%d)\n",avail_fuel);
        return false;
        }

    // Not enough karbonite
    if (cost_karb > avail_karb) 
        {
        System.out.printf ("Build failed: not enough karb (%d)\n",avail_karb);
        return false;
        }

    
    //Construct a new object
    Robot newR = null;
    int team = 0;
    
    //Red Team
    if (r.team == Specs.teamRed)
    {
        if (unit == Specs.unitChurch)   newR = new RedTeam.Church   ();
        if (unit == Specs.unitPilgrim)  newR = new RedTeam.Pilgrim  ();
        if (unit == Specs.unitCrusader) newR = new RedTeam.Crusader ();
        if (unit == Specs.unitProphet)  newR = new RedTeam.Prophet  ();
        if (unit == Specs.unitPreacher) newR = new RedTeam.Preacher ();
    }     
    
    if (r.team == Specs.teamBlue)
    {
        team = 1;
        if (unit == Specs.unitChurch)   newR = new BlueTeam.Church   ();
        if (unit == Specs.unitPilgrim)  newR = new BlueTeam.Pilgrim  ();
        if (unit == Specs.unitCrusader) newR = new BlueTeam.Crusader ();
        if (unit == Specs.unitProphet)  newR = new BlueTeam.Prophet  ();
        if (unit == Specs.unitPreacher) newR = new BlueTeam.Preacher ();
    }     
    
    if (newR != null) 
        {
        initRobot (newR, ++robot_count, team, unit, pos.x, pos.y);
        robots.add (newR);
        } else return false;

    //All is good, make adjustments
    adjTeamFuel (r.team, -cost_fuel); 
    adjTeamKarb (r.team, -cost_karb); 

    
    //Replace Log entry
    byte turn [] = {0,0,0,0};
    
    // 1 1 0 0 0 0 0 0  = C0
    // 0 0 1 0 1 0 0 0  = 28
    // 0 0 0 0 1 0 0 0  =  8
    // 0 0 0 0 0 1 0 1  =  5
    // 0 0 0 0 0 0 0 1  =  1
    
    turn[1]  = (byte)0xC0;
    turn[1] |= encodePos(r.action.dx,r.action.dy);
    turn[2] = new Integer (r.action.unit).byteValue();
    logTurn (turn);
    return true;
}

public static boolean processGive (Robot r)
{
    //- Must be adjacent
    //- Dst must have capacity
    int dx = r.action.dx;
    int dy = r.action.dy;
    int x  = r.x + dx;
    int y  = r.y + dy;
    int dist = dx*dx + dy*dy;
    Robot dst = findRobot (x,y);
    
    if (dist > 2)    {return false;}  
    if (dst == null) {return false;}
    
    //Check and transer fuel
    // Carry  Give  Capacity
    // - Give <= Capacity  Give = Capacity 
    int give_fuel  = r.action.fuel;
    int capacity   = Specs.getFuelCapacity(dst.unit) - dst.carry_fuel;
    if (dst.unit == Specs.unitCastle) capacity = give_fuel;
    if (give_fuel > capacity) give_fuel = capacity;
    //If a castle, add to team, else add to robot
    if (dst.unit == Specs.unitCastle) adjTeamFuel (r.team, give_fuel);
    else dst.carry_fuel += give_fuel;
        
    r.carry_fuel -= r.action.fuel; //All given, even if some is lost
    //-----------------------------------
    int give_karb  = r.action.karb;
    capacity   = Specs.getKarbCapacity(dst.unit) - dst.carry_karbonite;
    if (dst.unit == Specs.unitCastle) capacity = give_karb;
    if (give_karb > capacity) give_karb = capacity;
    if (dst.unit == Specs.unitCastle) adjTeamKarb (r.team, give_karb);
    else dst.carry_karbonite += give_karb;
    r.carry_karbonite -= r.action.karb;  //All given, even if some is lost
    //-----------------------------------
    
    byte turn [] = {1,0,0,0};
    // 1 0 0 0 0 0 0 0  = 80
    // 0 0 1 0 1 0 0 0  = 28
    // 0 0 0 0 1 0 0 0  =  8
    // 0 0 0 0 0 1 0 1  =  5
    // 0 0 0 0 0 0 0 1  =  1
    turn[1] = (byte)0x80;
    turn[1] |= encodePos (dx,dy);
    turn[2] = (byte)(give_karb);
    turn[3] = (byte)(give_fuel);
    logTurn (turn);
    return true;
}


//Save turn in supplied replay file
public static void logTurn (byte [] action)
{
    /*
    System.out.printf ("%02X|",  action[0]);
    System.out.printf ("%02X|",  action[1]);
    System.out.printf ("%02X|",  action[2]);
    System.out.printf ("%02X ",  action[3]);
    System.out.printf ("\n");
    */
    
    //If a replay file, then save it too
    if (replay == null) return;
    
    //If the first turn, then write the header
    byte pad [] = {0,0,0,0};
    if (turn == 1) 
        {
        //*HERE
        try { 
            replay.write((int)0);
            replay.write((int)0); 
            //Convert seed to byte buffer
            byte [] byte_seed = ByteBuffer.allocate(4).putInt(seed).array();
            replay.write(byte_seed); 
        } catch (IOException ex) 
            {
            Logger.getLogger(BattleCode2019.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    //Write out the move
    try 
    { 
        replay.write(pad); 
        replay.write(action); 
    } catch (IOException ex) 
            {
            Logger.getLogger(BattleCode2019.class.getName()).log(Level.SEVERE, null, ex);
            }
    
}



//Set team fuel levels
public static void setTeamFuel (int team_id, int fuel)
{
    if (fuel < 0) fuel = 0;
    team_fuel[team_id] = fuel;
} 


public static void adjTeamFuel (int team_id, int adj_fuel)
{
    team_fuel[team_id] += adj_fuel;
    if (team_fuel[team_id] < 0) team_fuel[team_id] = 0;
} 

public static void adjTeamKarb (int team_id, int adj_fuel)
{
    team_karb[team_id] += adj_fuel;
    if (team_karb[team_id] < 0) team_karb[team_id] = 0;
} 


public static int getTeamFuel (int team_id)
{
    return team_fuel[team_id];
} 

//Set team karb levels
public static void setTeamKarb (int team_id, int karb)
{
    if (karb < 0) karb = 0;
    team_karb[team_id] = karb;
} 

public static int getTeamKarb (int team_id)
{
    return team_karb[team_id];
} 

//Find the robot on the square provided
public static Robot getRobot (int x, int y)
{
    Robot r;
    for (int i=0; i<robots.size(); i++)
    {
        r = robots.get(i);
        if (r.x == x && r.y == y) return r;
    }
    return null;
}


//About to give r turn, populate with all values
public static void populate (Robot r)
{
    //maps are unchanged
    // map[][];  //Locations of passable (true) areas
    // karbonite_map [][];
    // fuel_map [][];
    // field_size[];
    
    r.karbonite = getTeamKarb(r.team);
    r.fuel      = getTeamFuel(r.team);
    
    //Adjust turn buffer
    r.turn  += 1;
    r.action.command = 0;
    
   
    //Location of robots
    //robot_map = 2D Array, true if robot present
    //visible_robots = List of visible robots 
    for (int row=0; row<map_size;row++)
        for (int col=0; col<map_size;col++)
            {
            r.robot_map[row][col] = false;
            r.map [row][col] = map[row][col];
            }
    //System.out.printf ("Map Size: %d", map_size);
    //System.out.printf ("Robot Map Size: %d", r.robot_map.length);
    
    r.visible_robots.clear();
    //if (r.unit == Specs.unitProphet) r.print();    

    for (int i=0; i<robots.size();i++)
        {    
        Robot r2 = robots.get(i);
        int dist = Nav.sqDist (r.getPos(),r2.getPos());
        //If a castle, everything is in vision
        if (r.unit == Specs.unitCastle || dist <= Specs.getVision(r.unit))
            {
            //If a profit 
            /*
                if (r.unit == Specs.unitProphet && r.team != r2.team) 
                {
                System.out.printf ("Dist:%d ", dist);
                r2.print();    
                }
            */
            r.visible_robots.add(r2); //Add to List
            r.robot_map[r2.y][r2.x] = true; 
            }
        }
    
}     

// -------------------------------
//Given a newly created robot, initialize it 
// -------------------------------
public static void initRobot (Robot r, int id, int team, int unit, int x, int y)
{   
    r.id   = id;
    r.team = team;  
    r.unit = unit;
    r.x    = x;
    r.y    = y;

    r.health = Specs.getInitHealth (unit);
    r.carry_fuel      = 0;         //Fuel currently carrying
    r.carry_karbonite = 0;
    
    //*HERE
    r.field_size    = map_size;
    r.map           = new boolean[map_size][map_size];
    r.karbonite_map = karb.clone();
    r.fuel_map      = fuel.clone();
    r.robot_map     = new boolean[map_size][map_size];
    
    //System.out.printf("Field Size: %d\n", map_size);
    
    r.karbonite = getTeamKarb(team);  //Amount of team carbonite
    r.fuel      = getTeamFuel(team);
}

// -------------------------------
// findRobot
// Given a position, iterate over visible
// robots finding robot at that position
// -------------------------------
public static Robot findRobot (int x, int y)
{   
    for (int i=0; i<robots.size (); i++)
        {
        Robot r = robots.get(i);
        if (r.x == x && r.y == y) return r;
        }
    return null;
}
 
//For testing purposes, displays field
public static void displayField ()
{
    //Construct image 4 times field size
    //Set all values to white
    //Draw 
    
}

//Used with the position is encoded in a single byte
public static byte encodePos (int x, int y)
{
    byte pos = 0;
    if (x == -1)  pos  |= (byte)0x28;
    if (x ==  1)  pos  |= (byte)0x08;
    if (y == -1)  pos  |= (byte)0x05;
    if (y ==  1)  pos  |= (byte)0x01;
    return pos;
}
    
}
