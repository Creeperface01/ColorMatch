<?php

namespace ColorMatch\Arena;

use pocketmine\scheduler\Task;
use pocketmine\tile\Sign;
use pocketmine\Player;
use ColorMatch\Arena\Arena;
use pocketmine\math\Vector3;

class ArenaSchedule extends Task{
    
    private $mainTime;
    private $time = 0;
    private $startTime;
    private $updateTime = 0;
    private $ending = false;
    
    private $forcestart = false;
    
    private $arena;
    
    #sign lines
    private $level;
    private $line1;
    private $line2;
    private $line3;
    private $line4;
    
    public function __construct(Arena $arena) {
        $this->arena = $arena;
        $this->startTime = $this->arena->data['arena']['starting_time'];
        $this->mainTime = $this->arena->data['arena']['max_game_time'];
        $this->line1 = str_replace("&", "§", $this->arena->data['signs']['status_line_1']);
        $this->line2 = str_replace("&", "§", $this->arena->data['signs']['status_line_2']);
        $this->line3 = str_replace("&", "§", $this->arena->data['signs']['status_line_3']);
        $this->line4 = str_replace("&", "§", $this->arena->data['signs']['status_line_4']);
        if(!$this->arena->plugin->getServer()->isLevelGenerated($this->arena->data['signs']['join_sign_world'])){
            $this->arena->plugin->getServer()->generateLevel($this->arena->data['signs']['join_sign_world']);
            $this->arena->plugin->getServer()->loadLevel($this->arena->data['signs']['join_sign_world']);
        }
        if(!$this->arena->plugin->getServer()->isLevelLoaded($this->arena->data['signs']['join_sign_world'])){
            $this->arena->plugin->getServer()->loadLevel($this->arena->data['signs']['join_sign_world']);
        }
    }
    
    public function onRun($currentTick){
        if(strtolower($this->arena->data['signs']['enable_status']) === 'true'){
            $this->updateTime++;
            if($this->updateTime === $this->arena->data['signs']['sign_update_time']){
                $vars = ['%alive', '%dead', '%status', '%type', '%max', '&'];
                $replace = [count(array_merge($this->arena->ingamep, $this->arena->lobbyp)), count($this->arena->deads), $this->arena->getStatus(), $this->arena->data['type'], $this->arena->getMaxPlayers(), "§"];
                $tile = $this->arena->plugin->getServer()->getLevelByName($this->arena->data['signs']['join_sign_world'])->getTile(new Vector3($this->arena->data['signs']['join_sign_x'], $this->arena->data['signs']['join_sign_y'], $this->arena->data['signs']['join_sign_z']));
                if($tile instanceof Sign){
                    $tile->setText(str_replace($vars, $replace, $this->line1), str_replace($vars, $replace, $this->line2), str_replace($vars, $replace, $this->line3), str_replace($vars, $replace, $this->line4));
                }
                $this->updateTime = 0;
            }
        }
        
        if($this->arena->game === 0){
            if(count($this->arena->lobbyp) >= $this->arena->getMinPlayers() || $this->forcestart === true){
                $this->startTime--;
                foreach($this->arena->lobbyp as $p){
                    $p->sendPopup(str_replace("%1", $this->startTime, $this->arena->plugin->getMsg('starting')));
                }
                if($this->startTime <= 0){
                    if(count($this->arena->lobbyp) >= $this->arena->getMinPlayers() || $this->forcestart === true){
                        $this->arena->startGame();
                        $this->startTime = $this->arena->data['arena']['starting_time'];
                        $this->forcestart = false;
                    }
                    else{
                        $this->startTime = $this->arena->data['arena']['starting_time'];
                    }
                }
            }
            else{
                $this->startTime = $this->arena->data['arena']['starting_time'];
            }
        }
        if($this->arena->game === 1){
            $this->startTime = $this->arena->data['arena']['starting_time'];
            $this->mainTime--;
            if($this->mainTime === 0){
                $this->arena->stopGame();
            }
            else{
                if($this->time == $this->arena->data['arena']['color_wait_time']){
                    $this->arena->removeAllExpectOne();
                }
                if($this->time == $this->arena->data['arena']['color_wait_time'] + 3){
                    $this->time = 0;
                    $this->arena->setColor(rand(0, 15));
                    $this->arena->resetFloor();
                }
                if(count($this->arena->ingamep) <= 1){
                    $this->arena->checkAlive();
                }
                $this->time++;
            }
        }
    }
}