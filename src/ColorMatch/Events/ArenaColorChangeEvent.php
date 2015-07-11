<?php

namespace ColorMatch\Events;

use pocketmine\event\plugin\PluginEvent;
use ColorMatch\ColorMatch;
use ColorMatch\Arena\Arena;
use pocketmine\event\Cancellable;

class PlayerJoinArenaEvent extends PluginEvent implements Cancellable{
    protected $arena;
    protected $oldcolor;
    protected $newcolor;
    
    public function __construct(ColorMatch $plugin, Arena $arena, $oldcolor, $newColor){
        parent::__construct($plugin);
        $this->player = $player;
        $this->arena = $arena;
    }
    
    
    public function getArena(){
        return $this->arena;
    }
    
    public function getArenaName(){
        return $this->arena->id;
    }
    
    public function getNewColor(){
        return $this->newcolor;
    }
    
    public function getOldColor(){
        return $this->oldcolor;
    }
    //color is 0-15
    public function setNewColor($color){
        $this->newcolor = $color;
    }
}