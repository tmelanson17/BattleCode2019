/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BCRobot;

// Helper class containing Specifications for robot 
// Values (like attackDamage, etc) should not be hard-coded
// Uses Specs class instead
public class Specs {
//Team
    public static int teamRed   = 0;
    public static int teamBlue  = 1;

//Unit types
    public static int unitCastle   = 0;
    public static int unitChurch   = 1;
    public static int unitPilgrim  = 2;
    public static int unitCrusader = 3;
    public static int unitProphet  = 4;
    public static int unitPreacher = 5;

//Commands 
    public static final int commandSkip   = 0;
    public static final int commandPass   = 0;
    public static final int commandMove   = 1;
    public static final int commandMine   = 2;
    public static final int commandGive   = 3;
    public static final int commandAttack = 4;
    public static final int commandBuild  = 5;
 
//Stats
    public static int getFuelBuildCost (int unit)
    {
        if (unit == unitChurch)   return 200;
        if (unit == unitPilgrim)  return  50;
        if (unit == unitCrusader) return  50;
        if (unit == unitProphet)  return  50;
        if (unit == unitPreacher) return  50;
        return 0;
    }

    public static int getFuelCapacity (int unit)
    {
        return  100;
    }

    public static int getKarbBuildCost (int unit)
    {            
        if (unit == unitChurch)   return  50;
        if (unit == unitPilgrim)  return  10;
        if (unit == unitCrusader) return  15;
        if (unit == unitProphet)  return  25;
        if (unit == unitPreacher) return  30;
        return 0;
    }

    public static int getKarbCapacity (int unit)
    {
        return  20;
    }

    public static int getInitHealth (int unit)
    {
        if (unit == unitCastle)   return 200;
        if (unit == unitChurch)   return 200;
        if (unit == unitPilgrim)  return  10;
        if (unit == unitCrusader) return  40;
        if (unit == unitProphet)  return  20;
        if (unit == unitPreacher) return  60;
        return 0;
    }

    public static int getVision (int unit)
    {
        if (unit == unitCastle)   return  0;
        if (unit == unitChurch)   return  0;
        if (unit == unitPilgrim)  return  100;
        if (unit == unitCrusader) return  49;
        if (unit == unitProphet)  return  64;
        if (unit == unitPreacher) return  16;
        return 0;
    }

    public static int getSpeed  (int unit)
    {
        if (unit == unitCastle)   return  0;
        if (unit == unitChurch)   return  0;
        if (unit == unitPilgrim)  return  4;
        if (unit == unitCrusader) return  9;
        if (unit == unitProphet)  return  4;
        if (unit == unitPreacher) return  4;
        return 0;
    }

    public static int getMoveFuel (int unit)
    {
        if (unit == unitCastle)   return  0;
        if (unit == unitChurch)   return  0;
        if (unit == unitPilgrim)  return  1;
        if (unit == unitCrusader) return  1;
        if (unit == unitProphet)  return  2;
        if (unit == unitPreacher) return  3;
        return 0;
    }
    
    public static int getAttackFuel (int unit)
    {
        if (unit == unitCastle)   return  0;
        if (unit == unitChurch)   return  0;
        if (unit == unitPilgrim)  return  0;
        if (unit == unitCrusader) return  10;
        if (unit == unitProphet)  return  25;
        if (unit == unitPreacher) return  15;
        return 0;
    }

    public static int getAttackMin (int unit)
    {
        if (unit == unitCastle)   return  0;
        if (unit == unitChurch)   return  0;
        if (unit == unitPilgrim)  return  0;
        if (unit == unitCrusader) return  1;
        if (unit == unitProphet)  return  16;
        if (unit == unitPreacher) return  1;
        return 0;
    }

    public static int getAttackMax (int unit)
    {
        if (unit == unitCastle)   return  0;
        if (unit == unitChurch)   return  0;
        if (unit == unitPilgrim)  return  0;
        if (unit == unitCrusader) return  16;
        if (unit == unitProphet)  return  64;
        if (unit == unitPreacher) return  16;
        return 0;
    }

    public static int getAttackDamage (int unit)
    {
        if (unit == unitCastle)   return  0;
        if (unit == unitChurch)   return  0;
        if (unit == unitPilgrim)  return  0;
        if (unit == unitCrusader) return  10;
        if (unit == unitProphet)  return  10;
        if (unit == unitPreacher) return  20;
        return 0;
    }

    public static String getUnitName (int unit)
    {
        if (unit == unitCastle)   return  "Castle";
        if (unit == unitChurch)   return  "Church";
        if (unit == unitPilgrim)  return  "Pilgrim";
        if (unit == unitCrusader) return  "Crusader";
        if (unit == unitProphet)  return  "Prophet";
        if (unit == unitPreacher) return  "Preacher";
        return "Unknown";
    }
    public static String getTeamName (int team)
    {
        if (team == teamRed)      return  "Red";
        if (team == teamBlue)     return  "Blue";
        return "Unknown";
    }
    
    public static String getCommandName (int command)
    {
        switch (command)
        {
            case commandPass:   return "Pass";
            case commandMove:   return "Move";
            case commandMine:   return "Mine";
            case commandGive:   return "Give";
            case commandAttack: return "Attack";
            case commandBuild:  return "Build";
            default: return "Unknown";
        }
    }

}
