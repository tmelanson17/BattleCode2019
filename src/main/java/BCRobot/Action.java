/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BCRobot;


//Action is a storage container that is set by 
//each Robot action (move, give, etc) with the parameters
//of the action
public class Action 
{
    public int command;   //ID of move to make
    public int dx,dy;     //Offset to target robot
    public int fuel;      //If fuel is param in command (ie give)
    public int karb;      //If karb is param in command (ie give)
    public int unit;      //If unit is param in command (ie build)
}
