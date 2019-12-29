/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BCRobot;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//Nav is a ported version of Javascript nav, except using Java types
//Specifically locations are type 'Point' 

public class Nav {
    public static String compass   [][] = {{"NW", "N", "NE"},{"W", "C", "E"},{"SW", "S", "SE"}};
    public static String rotateArr [] = {"N", "NE", "E","SE", "S", "SW", "W", "NW"};
    public static int    rotateTry [] = {1, -1, 2, -2, 3, -3, 4};
    
    //Create and populate array
    public static Map <String,Integer> rotateArrInd = new HashMap <String,Integer>()
    {  
        {
        put("N",  0);
        put("NE", 1);
        put("E",  2);
        put("SE", 3);
        put("S",  4);
        put("SW", 5);
        put("W",  6);
        put("NW", 7);
        }
    };    

    
    public static Map <String,Point> compassToCoordinate = new HashMap <String,Point>()
    {  
        {
        put("N",  new Point ( 0, -1));
        put("NE", new Point ( 1, -1));
        put("NW", new Point (-1, -1));
        put("E",  new Point ( 1,  0));
        put("W",  new Point (-1,  0));
        put("S",  new Point ( 0,  1));
        put("SE", new Point ( 1,  1));
        put("SW", new Point (-1,  1));
        }
    };    
    
    public static ArrayList  <Point> offsetList = new ArrayList <Point>()
    {  
        {
        add(new Point ( 0, -1));
        add(new Point ( 1, -1));
        add(new Point (-1, -1));
        add(new Point ( 1,  0));
        add(new Point (-1,  0));
        add(new Point ( 0,  1));
        add(new Point ( 1,  1));
        add(new Point (-1,  1));
        }
    };  
    
    public static String toCompassDir (Point dir)
    {
        return compass [dir.y+1][dir.x+1];
    }
    
    public static  Point toCoordinateDir (String dir)
    {
        return compassToCoordinate.get(dir);
    }
    
    //Sample calculation
    // dir = -1,0 
    // amount = 1
    // compassDir =  W
    // rotateArrInd[compassDir]   = 6 
    // + amount                   = 7 
    // + rotateArr.length         = 7 + 8 = 15
    // % nav.rotateArr.length     = 15 % 8 = 7 = NW; 
    // Return -1,1
    public static Point rotate (Point dir, int amount)
    {
        String compassDir = toCompassDir (dir);
        //rotateCompassDir steps.  Adding rotateArr.length
        //is probably to make sure number is positive
        // amount                     = 1
        // dir                        = 1,0
        // compassDir                 = E
        // rotateArrInd[compassDir]   = 2 
        // + amount                   = 3 
        // + rotateArr.length         = 3  + 8 = 11
        // % nav.rotateArr.length     = 11 % 8 = 3 = SW; 
        String rotateCompassDir = rotateArr[(rotateArrInd.get(compassDir) + amount + rotateArr.length) % rotateArr.length]; 
        return toCoordinateDir(rotateCompassDir);
    }
    
    public static Point reflect (Point loc, int mapLen, boolean isHorizontalReflection)
    {
        Point hReflect = new Point (loc.x, mapLen - loc.y - 1);
        Point vReflect = new Point (mapLen - loc.x - 1, loc.y);
        return (isHorizontalReflection ? hReflect : vReflect); 
    }    

    public static boolean isHoReflect  (boolean map[][])
    {
        // self.log('starting reflect check');
        int mapLen = map.length;  //X length (same as y length)
        boolean Plausible = true;
        for (int y = 0; y < mapLen && Plausible; y++) 
            {
            for (int x = 0; x < mapLen && Plausible; x++) 
                {
                Plausible = (map[y][x] == map[mapLen - y - 1][x]);
                }
            }    
        return Plausible;
    }
// start  = 42, 43
// target =  7, 43
// dir    = -1, -1

public static Point getDir (Point start, Point target)
    {
    //newDir = -35, 0
    Point newDir = new Point (target.x - start.x, target.y - start.y);
    //newDir.x = -1;
    if (newDir.x < 0) {
        newDir.x = -1;
    } else if (newDir.x > 0) {
        newDir.x = 1;
    }

    //newDir.y = 0;
    if (newDir.y < 0) {
        newDir.y = -1;
    } else if (newDir.y > 0) {
        newDir.y = 1;
    }

    return newDir;
};

public static boolean isPassable (Point loc, boolean map[][], boolean robot_map[][])
    {
    int x = loc.x;
    int y = loc.y;
    //System.out.printf("%d,%d: passable:%b, robot: %b\n", x,y,map[y][x],robot_map[y][x]);
    
    int mapLen = map.length;
    if (x >= mapLen || x < 0) {return false;}
    if (y >= mapLen || y < 0) {return false;}
    if (robot_map[y][x])      {return false;}
    if (!map[y][x])           {return false;} 
    
    //System.out.printf("Passable!\n");
    return true;
    }

public static Point applyDir (Point loc, Point dir)
    {
    return new Point(loc.x + dir.x,loc.y + dir.y);
    };

//goto is a reserved word 
//Return is a direction to move towards dest
// start = 42, 43
// dest  =  7, 43
// dir   = -1, -1
public static Point gotoDir (Point start, Point dest, boolean fullMap[][], boolean robotMap[][])
    {
    Point goalDir = getDir (start, dest);
    if (goalDir.x == 0 && goalDir.y == 0) {return goalDir;}
    //tryDir = -1,0
    Point tryDir = new Point(goalDir);
    int ind = 0;
    //tryDir = -1,0  No  ind = 0;
    //tryDir = 
    while (!isPassable (
            applyDir (start, tryDir),
            fullMap,
            robotMap) && ind < rotateTry.length) {
        tryDir = rotate(goalDir, rotateTry[ind]);
        ind++;
        }
    return tryDir;
};

public static int sqDist (Point start, Point end)
{
    int dx = start.x - end.x;
    int dy = start.y - end.y;
    return (dx*dx + dy*dy);
};

public static Point getClosestRsrc (Point loc, boolean rsrcMap[][])
{
    int mapLen = rsrcMap.length;
    Point closestLoc  = null;
    int   closestDist = 100000; // Large number;
    for (int y = 0; y < mapLen; y++) 
        {
        for (int x = 0; x < mapLen; x++) 
            {
            Point target = new Point (x,y);
            if (rsrcMap[y][x] && sqDist(target, loc) < closestDist) 
                {
                closestDist = sqDist(target, loc);
                closestLoc  = (Point)(target.clone());
                }
            }
        }
    return closestLoc;
};
    
}

