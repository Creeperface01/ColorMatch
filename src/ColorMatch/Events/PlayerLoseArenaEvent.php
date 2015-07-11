<?php

namespace ColorMatch\Events;

use pocketmine\event\plugin\PluginEvent;
use pocketmine\Player;
use ColorMatch\ColorMatch;
use ColorMatch\Arena\Arena;

class PlayerLoseArenaEvent extends PluginEvent{
    protected $player;
    protected $arena;
    
    public function __construct(ColorMatch $plugin, Player $player, Arena $arena){
        parent::__construct($plugin);
        $this->player = $player;
        $this->arena = $arena;
    }
    
    public function getPlayer(){
        return $this->player;
    }
    
    public function getArena(){
        return $this->arena;
    }
    
    public function getArenaName(){
        return $this->arena->id;
    }
}