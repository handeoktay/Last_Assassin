package com.ngdroidapp;

import android.graphics.Canvas;
import android.graphics.Point;

import com.ngdroidapp.GameObjects.Background;
import com.ngdroidapp.GameObjects.Foreground;
import com.ngdroidapp.GameObjects.Ground;
import com.ngdroidapp.GameObjects.HUD;
import com.ngdroidapp.GameObjects.Player;

import istanbul.gamelab.ngdroid.base.BaseCanvas;
import istanbul.gamelab.ngdroid.util.Log;

public class GameCanvas extends BaseCanvas{

    private Background background;
    private Foreground foreground;
    private Ground ground;
    private Player player;
    private HUD hud;

    private TouchControl touchControl;

    @SuppressWarnings("WeakerAccess")
    public GameCanvas(NgApp ngApp){
        super(ngApp);
    }

    public void setup(){
        Log.i(TAG, "setup");

        touchControl = new TouchControl();

        background = new Background("Level_1_Background.png", root, touchControl);
        foreground = new Foreground("Level_1_Foreground.png", root, background);
        hud = new HUD(root);
        ground = new Ground(background, hud, touchControl, getWidth(), getHeight());
        player = new Player("ninja.png", root, background, ground);

        //Setting the player's coordinates.
        player.setCoordinates((getWidth() / 4) - player.getDestinationWidth(), (int) (getHeight() / 1.7) - player.getDestinationHeight());
    }

    public void update(){

        background.update(player, hud, touchControl.isDpadPressing());
        player.update();
        foreground.update(player.getSpeed(), player.isMoving());

    }

    public void draw(Canvas canvas){

        //Log.i(TAG, "draw");
        background.draw(canvas);
        ground.draw(canvas);
        player.draw(canvas);
        foreground.draw(canvas);
        hud.draw(canvas);

        //FPS
        root.gui.drawText(canvas, "FPS: " + root.appManager.getFrameRate() + " / " + root.appManager.getFrameRateTarget(),
                getWidth() - (int) (getWidth() / 5.5), getHeight() / 16, 0);
    }

    public void touchDown(int x, int y, int id){

        //Accepts two fingers at most.
        if(id < 2){

            //If the user touches to DPAD and is not touching to DPAD with the other ID.
            if(hud.checkAnyCollisionDPad(x, y) && !touchControl.isOtherTouchDpad(id)){

                //Lastly, we need to check if the button is editable.
                if(hud.returnButton(hud.pressedButtonDPad(x, y)).isEditable()){
                    player.dpadPressed(hud.pressedButtonDPad(x, y), hud);
                    touchControl.addTouch(id, new Point(x, y), true, false);
                }
            }
            //If the user touches to Action Buttons and is not touching to Action Buttons with the other ID.
            else if(hud.checkAnyCollisionActions(x, y) && !touchControl.isOtherTouchAction(id)){

                if(hud.returnButton(hud.pressedButtonActions(x, y)).isEditable()){
                    player.actionsPressed(hud.pressedButtonActions(x, y), hud);
                    touchControl.addTouch(id, new Point(x, y), false, true);
                }
            }
        }
    }

    public void touchMove(int x, int y, int id){

        //Accepts two fingers at most. And if that ID exists...
        if(id < 2 && touchControl.doesExist(id)){

            //If player is not hovering over the D-Pad button...
            if(touchControl.getDpadPressed(id) && !touchControl.getActionButtonPressed(id))
                if(!hud.checkCollisionDPad(hud.pressedButtonDPad(touchControl.getTouch(id).x, touchControl.getTouch(id).y), x, y)){

                    player.setMoving(false);
                    hud.scaleEverythingToSmallDPad(1.2);
                    touchControl.updateTouch(id, -100, -100);
                }

            //Continue movement if user is hovering over on DPad and getDpadPressed is true and player is not moving.
            if(touchControl.getDpadPressed(id) && !player.isMoving())
                if(hud.checkAnyCollisionDPad(x, y) && hud.returnButton(hud.pressedButtonDPad(x, y)).isEditable()){

                    player.dpadPressed(hud.pressedButtonDPad(x, y), hud);
                    touchControl.updateTouch(id, x, y);
                }
        }
    }

    public void touchUp(int x, int y, int id){

        if(id < 2 && touchControl.doesExist(id)){

            //If the user releases the DPAD...
            if(touchControl.getDpadPressed(id)){
                hud.revertScaleToOriginalDPad();
                player.setMoving(false);
            }
            //If the user releases the Action Buttons...
            else if(touchControl.getActionButtonPressed(id))
                hud.revertScaleToOriginalActions();

            //Removes the touch.
            touchControl.removeTouch(id);
        }
    }

    public boolean backPressed(){

        System.exit(0);
        return true;
    }
}