<?php

namespace ColorMatch;

use pocketmine\plugin\PluginBase;
use pocketmine\command\Command;
use pocketmine\command\CommandSender;
use pocketmine\utils\Config;
use pocketmine\utils\TextFormat;
use pocketmine\Player;
use ColorMatch\Arena\Arena;
use pocketmine\event\Listener;
use pocketmine\event\player\PlayerInteractEvent;
use pocketmine\item\Item;
use pocketmine\event\block\BlockBreakEvent;
use pocketmine\event\block\BlockPlaceEvent;
use pocketmine\level\Position;
use pocketmine\event\player\PlayerJoinEvent;
use pocketmine\event\player\PlayerChatEvent;
use pocketmine\event\player\PlayerQuitEvent;
use pocketmine\event\player\PlayerKickEvent;
use pocketmine\plugin\Plugin;

class ColorMatch extends PluginBase implements Listener{

    public $cfg;
    public $msg;
    public $arenas = [];
    public $ins = [];
    public $selectors = [];
    public $inv = [];
    public $setters = [];
    
    public $economy;
    
    public function onEnable(){
        $this->initConfig();
        $this->registerEconomy();
        $this->checkArenas();
        $this->getServer()->getPluginManager()->registerEvents($this, $this);
        if(!$this->getServer()->isLevelGenerated($this->cfg->getNested('lobby.world'))){
            $this->getServer()->generateLevel($this->cfg->getNested('lobby.world'));
        }
        $this->getLogger()->info(TextFormat::GREEN."ColorMatch enabled");
    }
    
    public function onDisable(){
        $this->getLogger()->info(TextFormat::RED."ColorMatch disabled");
    }
    
    public function setArenasData(Config $arena, $name){
        $this->arenas[$name] = $arena->getAll();
        $this->arenas[$name]['enable'] = true;
        $game = new Arena($name, $this);
        $game->enableScheduler();
        $this->ins[$name] = $game;
        $this->getServer()->getPluginManager()->registerEvents($game, $this);
    }
    
    public function initConfig(){
        if(!file_exists($this->getDataFolder())){
            @mkdir($this->getDataFolder());
        }
        if(!is_file($this->getDataFolder()."config.yml")){
            $this->saveResource("config.yml");
        }
        $this->cfg = new Config($this->getDataFolder()."config.yml", Config::YAML);
        if(!file_exists($this->getDataFolder()."arenas/")){
            @mkdir($this->getDataFolder()."arenas/");
            $this->saveResource("arenas/default.yml");
        }
        if(!file_exists($this->getDataFolder()."languages/")){
            @mkdir($this->getDataFolder()."languages/");
        }
        if(!is_file($this->getDataFolder()."languages/English.yml")){
                $this->saveResource("languages/English.yml");
        }
        if(!is_file($this->getDataFolder()."languages/Czech.yml")){
                $this->saveResource("languages/Czech.yml");
        }
        if(!is_file($this->getDataFolder()."languages/{$this->cfg->get('Language')}.yml")){
            $this->msg = new Config($this->getDataFolder()."languages/English.yml", Config::YAML);
            $this->getServer()->getLogger()->info("Selected language English");
        }
        else{
            $this->msg = new Config($this->getDataFolder()."languages/{$this->cfg->get('Language')}.yml", Config::YAML);
            $this->getServer()->getLogger()->info("Selected language {$this->cfg->get('Language')}");
        }
    }
    
    public function checkArenas(){
        $this->getLogger()->info("checking arena files...");
        foreach(glob($this->getDataFolder()."arenas/*.yml") as $file){
            $arena = new Config($file, Config::YAML);
            if(strtolower($arena->get("enabled")) === "false"){
                $this->arenas[basename($file, ".yml")] = $arena->getAll();
                $this->arenas[basename($file, ".yml")]['enable'] = false;
            }
            else{
                if($this->checkFile($arena)){
                    $fname = basename($file);
                    $this->setArenasData($arena, basename($file, ".yml"));
                    $this->getLogger()->info("$fname - ".TextFormat::GREEN."checking sucessful");
                }
                else{
                    $this->arenas[basename($file, ".yml")] = $arena->getAll();
                    $this->arenas[basename($file, ".yml")]['enable'] = false;
                    //$this->setArenasData($arena, basename($file, ".yml"), false);
                    $fname = basename($file, ".yml");
                    $this->getLogger()->error("Arena \"$fname\" is not set properly");
                }
            }
        }
    }
    
    public function checkFile(Config $arena){
        if(!(is_numeric($arena->getNested("signs.join_sign_x")) && is_numeric($arena->getNested("signs.join_sign_y")) && is_numeric($arena->getNested("signs.join_sign_z")) && is_string($arena->getNested("signs.join_sign_world")) && is_string($arena->getNested("signs.status_line_1")) && is_string($arena->getNested("signs.status_line_2")) && is_string($arena->getNested("signs.status_line_3")) && is_string($arena->getNested("signs.status_line_4")) && is_numeric($arena->getNested("signs.return_sign_x")) && is_numeric($arena->getNested("signs.return_sign_y")) && is_numeric($arena->getNested("signs.return_sign_z")) && is_string($arena->getNested("arena.arena_world")) && is_numeric($arena->getNested("arena.join_position_x")) && is_numeric($arena->getNested("arena.join_position_y")) && is_numeric($arena->getNested("arena.join_position_z")) && is_numeric($arena->getNested("arena.lobby_position_x")) && is_numeric($arena->getNested("arena.lobby_position_y")) && is_numeric($arena->getNested("arena.lobby_position_z")) && is_numeric($arena->getNested("arena.first_corner_x")) && is_numeric($arena->getNested("arena.first_corner_z")) && is_numeric($arena->getNested("arena.second_corner_x")) && is_numeric($arena->getNested("arena.second_corner_z")) && is_numeric($arena->getNested("arena.spec_spawn_x")) && is_numeric($arena->getNested("arena.spec_spawn_y")) && is_numeric($arena->getNested("arena.spec_spawn_z")) && is_numeric($arena->getNested("arena.leave_position_x")) && is_numeric($arena->getNested("arena.leave_position_y")) && is_numeric($arena->getNested("arena.leave_position_z")) && is_string($arena->getNested("arena.leave_position_world")) && is_numeric($arena->getNested("arena.max_game_time")) && is_numeric($arena->getNested("arena.max_players")) && is_numeric($arena->getNested("arena.min_players")) && is_numeric($arena->getNested("arena.starting_time")) && is_numeric($arena->getNested("arena.color_wait_time")) && is_numeric($arena->getNested("arena.floor_y")) && is_string($arena->getNested("arena.finish_msg_levels")) && is_string($arena->getNested("arena.item_reward")) && !is_string($arena->getNested("arena.money_reward")))){
            return false;
        }
        if(!((strtolower($arena->get("type")) == "furious" || strtolower($arena->get("type")) == "stoned" || strtolower($arena->get("type")) == "classic") && (strtolower($arena->get("material")) == "wool" || strtolower($arena->get("material")) == "clay") && (strtolower($arena->getNested("signs.enable_status")) == "true" || strtolower($arena->getNested("signs.enable_status")) == "false") && (strtolower($arena->getNested("arena.spectator_mode")) == "true" || strtolower($arena->getNested("arena.spectator_mode")) == "false") && (strtolower($arena->getNested("arena.time")) == "true" || strtolower($arena->getNested("arena.time")) == "day" || strtolower($arena->getNested("arena.time")) == "night" || is_numeric(strtolower($arena->getNested("arena.time")))) && (strtolower($arena->get("enabled")) == "true" || strtolower($arena->get("enabled")) == "false"))){
            return false;
        }
        return true;
    }
    
    public function onCommand(CommandSender $sender, Command $cmd, $label, array $args){
            if(strtolower($cmd->getName()) == "cm"){
                    if(isset($args[0])){
                        if($sender instanceof Player){
                        switch(strtolower($args[0])){
                            case "lobby":
                                if(!$sender->hasPermission('cm.command.lobby')){
                                    $sender->sendMessage($this->getMsg('has_not_permission'));
                                    break;
                                }
                                if($this->getPlayerArena($sender) !== false){
                                    $this->getPlayerArena($sender)->leaveArena($sender);
                                    break;
                                }
                                $sender->teleport(new Position($this->cfg->getNested('lobby.x'), $this->cfg->getNested('lobby.y'), $this->cfg->getNested('lobby.z'), $this->getServer()->getLevelByName($this->cfg->getNested('lobby.world'))));
                                $sender->sendMessage($this->getPrefix().$this->getMsg('send_to_main_world'));
                                break;
                            case "set":
                                if(!$sender->hasPermission('cm.command.set')){
                                    $sender->sendMessage($this->getMsg('has_not_permission'));
                                    break;
                                }
                                if(!isset($args[1]) || isset($args[2])){
                                    $sender->sendMessage($this->getPrefix().$this->getMsg('set_help'));
                                    break;
                                }
                                if(!$this->arenaExist($args[1])){
                                    $sender->sendMessage($this->getPrefix().$this->getMsg('arena_doesnt_exist'));
                                    break;
                                }
                                if($this->isArenaSet($args[1])){
                                    $a = $this->ins[$args[1]];
                                    if($a->game !== 0 || count(array_merge($a->ingamep, $a->lobbyp, $a->spec)) > 0){
                                        $sender->sendMessage($this->getPrefix().$this->getMsg('arena_running'));
                                        break;
                                    }
                                    $a->setup = true;
                                }
                                $this->setters[strtolower($sender->getName())]['arena'] = $args[1];
                                $sender->sendMessage($this->getPrefix().$this->getMsg('enable_setup_mode'));
                                break;
                            case "help":
                                if(!$sender->hasPermission("cm.command.help")){
                                    $sender->sendMessage($this->getMsg('has_not_permission'));
                                    break;
                                }
                                $msg = "§9--- §c§lColorMatch help§l§9 ---§r§f";
                                if($sender->hasPermission('cm.command.lobby')) $msg .= $this->getMsg('lobby');
                                if($sender->hasPermission('cm.command.leave')) $msg .= $this->getMsg('onleave');
                                if($sender->hasPermission('cm.command.join')) $msg .= $this->getMsg('onjoin');
                                if($sender->hasPermission('cm.command.start')) $msg .= $this->getMsg('start');
                                if($sender->hasPermission('cm.command.stop')) $msg .= $this->getMsg('stop');
                                if($sender->hasPermission('cm.command.kick')) $msg .= $this->getMsg('kick');
                                if($sender->hasPermission('cm.command.set')) $msg .= $this->getMsg('set');
                                if($sender->hasPermission('cm.command.delete')) $msg .= $this->getMsg('delete');
                                if($sender->hasPermission('cm.command.create')) $msg .= $this->getMsg('create');
                                $sender->sendMessage($msg);
                                break;
                            case "create":
                                if(!$sender->hasPermission('cm.command.create')){
                                    $sender->sendMessage($this->getMsg ('has_not_permission'));
                                    break;
                                }
                                if(!isset($args[1]) || isset($args[2])){
                                    $sender->sendMessage($this->getPrefix().$this->getMsg('create_help'));
                                    break;
                                }
                                if($this->arenaExist($args[1])){
                                    $sender->sendMessage($this->getPrefix().$this->getMsg('arena_already_exist'));
                                    break;
                                }
                                $a = new Config($this->getDataFolder()."arenas/$args[1].yml", Config::YAML);
                                file_put_contents($this->getDataFolder()."arenas/$args[1].yml", $this->getResource('arenas/default.yml'));
                                $this->arenas[$args[1]] = $a->getAll();
                                $sender->sendMessage($this->getPrefix().$this->getMsg('arena_create'));
                                break;
                            case "delete":
                                if(!$sender->hasPermission('cm.command.delete')){
                                    $sender->sendMessage($this->getMsg ('has_not_permission'));
                                    break;
                                }
                                if(!isset($args[1]) || isset($args[2])){
                                    $sender->sendMessage($this->getPrefix().$this->getMsg('delete_help'));
                                    break;
                                }
                                if(!$this->arenaExist($args[1])){
                                    $sender->sendMessage($this->getPrefix().$this->getMsg('arena_doesnt_exist'));
                                    break;
                                }
                                unlink($this->getDataFolder()."arenas/$args[1].yml");
                                unset($this->arenas[$args[1]]);
                                $sender->sendMessage($this->getPrefix().$this->getMsg('arena_delete'));
                                break;
                            case "join":
                                if(!$sender->hasPermission('cm.command.join')){
                                    $sender->sendMessage($this->getMsg('has_not_permission'));
                                    break;
                                }
                                if(!isset($args[1]) || isset($args[2])){
                                    $sender->sendMessage($this->getPrefix().$this->getMsg('join_help'));
                                    break;
                                }
                                if(!$this->arenaExist($args[1])){
                                    $sender->sendMessage($this->getPrefix().$this->getMsg('arena_doesnt_exist'));
                                    break;
                                }
                                if($this->arenas[$args[1]]['enable'] === false){
                                    $sender->sendMessage($this->getPrefix().$this->getMsg('arena_doesnt_exist'));
                                    break;
                                }
                                $this->ins[$args[1]]->joinToArena($sender);
                                break;
                            case "leave":
                                if(!$sender->hasPermission('cm.command.leave')){
                                    $sender->sendMessage($this->getMsg ('has_not_permission'));
                                    break;
                                }
                                if(isset($args[1])){
                                    $sender->sendMessage($this->getPrefix().$this->getMsg('leave_help'));
                                    break;
                                }
                                if($this->getPlayerArena($sender) === false){
                                    $sender->sendMessage($this->getPrefix().$this->getMsg('use_cmd_in_game'));
                                    break;
                                }
                                $this->getPlayerArena($sender)->leaveArena($sender);
                                break;
                            case "start":
                                if(!$sender->hasPermission('cm.command.start')){
                                    $sender->sendMessage($this->plugin->getMsg('has_not_permission'));
                                    break;
                                }
                                if(!isset($args[1]) || isset($args[2])){
                                    $sender->sendMessage($this->getPrefix().$this->getMsg('start_help'));
                                    break;
                                }
                                if(!isset($this->ins[$args[1]])){
                                    $sender->sendMessage($this->getPrefix().$this->getMsg('arena_doesnt_exist'));
                                    break;
                                }
                                $this->ins[$args[1]]->startGame();
                                break;
                            case "stop":
                                if(!$sender->hasPermission('cm.command.start')){
                                    $sender->sendMessage($this->plugin->getMsg('has_not_permission'));
                                    break;
                                }
                                if(!isset($args[1]) || isset($args[2])){
                                    $sender->sendMessage($this->getPrefix().$this->getMsg('stop_help'));
                                    break;
                                }
                                if(!isset($this->ins[$args[1]])){
                                    $sender->sendMessage($this->getPrefix().$this->getMsg('arena_doesnt_exist'));
                                    break;
                                }
                                if($this->ins[$args[1]->game !== 1]){
                                    $sender->sendMessage($this->getPrefix().$this->getMsg('arena_not_running'));
                                    break;
                                }
                                $this->ins[$args[1]]->stopGame();
                                break;
                            //TO-DO case "ban":
                            case "kick": // cm kick [arena] [player] [reason]
                                if(!$sender->hasPermission('cm.command.kick')){
                                    $sender->sendMessage($this->getMsg('has_not_permission'));
                                    break;
                                }
                                if(!isset($args[2]) || isset($args[4])){
                                    $sender->sendMessage($this->getPrefix().$this->getMsg('kick_help'));
                                    break;
                                }
                                if(!isset(array_merge($this->ins[$args[1]]->ingamep, $this->ins[$args[1]]->lobbyp, $this->ins[$args[1]]->spec)[strtolower($args[2])])){
                                    $sender->sendMessage($this->getPrefix().$this->getMsg('player_not_exist'));
                                    break;
                                }
                                if(!isset($args[3])){
                                    $args[3] = "";
                                }
                                $this->ins[$args[1]]->kickPlayer($args[2], $args[3]);
                                break;
                            case "setlobby":
                                if(!$sender->hasPermission('cm.command.setlobby')){
                                    $sender->sendMessage($this->getMsg('has_not_permission'));
                                    break;
                                }
                                if(isset($args[1])){
                                    $sender->sendMessage($this->getPrefix().$this->getMsg('setlobby_help'));
                                    break;
                                }
                                $this->setters[strtolower($sender->getName())]['type'] = "mainlobby";
                                $sender->sendMessage($this->getPrefix().$this->getMsg('break_block'));
                                break;
                            default:
                                $sender->sendMessage($this->getPrefix().$this->getMsg('help'));
                        }
                        return;
                        }
                        $sender->sendMessage('run command only in-game');
                        return;
                    }
                    $sender->sendMessage($this->getPrefix().$this->getMsg('help'));
            }
    }
    
    public function arenaExist($name){
        if(isset($this->arenas[$name])){
            return true;
        }
        return false;
    }
    
    public function getMsg($key){
        $msg = $this->msg;
        return str_replace("&", "§", $msg->get($key));
    }
    
    public function onBlockTouch(PlayerInteractEvent $e){
        $p = $e->getPlayer();
        $b = $e->getBlock();
        if(isset($this->selectors[strtolower($p->getName())])){
            $p->sendMessage(TextFormat::BLUE."X: ".TextFormat::GREEN.$b->x.TextFormat::BLUE." Y: ".TextFormat::GREEN.$b->y.TextFormat::BLUE." Z: ".TextFormat::GREEN.$b->z);
        }
    }
    
    public function getPrefix(){
        return str_replace("&", "§", $this->cfg->get('Prefix'));
    }
    
    public function loadInvs(){
        foreach($this->getServer()->getOnlinePlayers() as $p){
            if(isset($this->inv[strtolower($p->getName())])){
                foreach($this->inv as $slot => $i){
                    list($id, $dmg, $count) = explode(":", $i);
                    $item = Item::get($id, $dmg, $count);
                    $p->getInventory()->setItem($slot, $item);
                    unset($this->plugin->inv[strtolower($p->getName())]);
                }
            }
        }
    }
    
    public function onBlockBreak(BlockBreakEvent $e){
        $p = $e->getPlayer();
        //for freezecraft only
        /*if(!$p->isOp()){
            $e->setCancelled(true);
        }*/
        if(isset($this->setters[strtolower($p->getName())]['arena']) && isset($this->setters[strtolower($p->getName())]['type'])){
            $e->setCancelled(true);
            $b = $e->getBlock();
            $arena = new ConfigManager($this->setters[strtolower($p->getName())]['arena'], $this);
            if($this->setters[strtolower($p->getName())]['type'] == "setjoinsign"){
                $arena->setJoinSign($b->x, $b->y, $b->z, $b->level->getName());
                $p->sendMessage($this->getPrefix().$this->getMsg('joinsign'));
                return;
            }
            if($this->setters[strtolower($p->getName())]['type'] == "setreturnsign"){
                $arena->setReturnSign($b->x, $b->y, $b->z);
                $p->sendMessage($this->getPrefix().$this->getMsg('returnsign'));
                return;
            }
            if($this->setters[strtolower($p->getName())]['type'] == "setjoinpos"){
                $arena->setJoinPos($b->x, $b->y, $b->z);
                $p->sendMessage($this->getPrefix().$this->getMsg('startpos'));
                return;
            }
            if($this->setters[strtolower($p->getName())]['type'] == "setlobbypos"){
                $arena->setLobbyPos($b->x, $b->y, $b->z);
                $p->sendMessage($this->getPrefix().$this->getMsg('lobbypos'));
                return;
            }
            if($this->setters[strtolower($p->getName())]['type'] == "setfirstcorner"){
                $arena->setFirstCorner($b->x, $b->y, $b->z);
                $p->sendMessage($this->getPrefix().$this->getMsg('first_corner'));
                $this->setters[strtolower($p->getName())]['type'] = "setsecondcorner";
                return;
            }
            if($this->setters[strtolower($p->getName())]['type'] == "setsecondcorner"){
                $arena->setSecondCorner($b->x, $b->z);
                $p->sendMessage($this->getPrefix().$this->getMsg('second_corner'));
                return;
            }
            if($this->setters[strtolower($p->getName())]['type'] == "setspecspawn"){
                $arena->setSpecSpawn($b->x, $b->y, $b->z);
                $p->sendMessage($this->getPrefix().$this->getMsg('spectatorspawn'));
                return;
            }
            if($this->setters[strtolower($p->getName())]['type'] == "setleavepos"){
                $arena->setLeavePos($b->x, $b->y, $b->z, $b->level->getName());
                $p->sendMessage($this->getPrefix().$this->getMsg('leavepos'));
                return;
            }
            if($this->setters[strtolower($p->getName())]['type'] == "mainlobby"){
                $this->cfg->setNested("lobby.x", $b->x);
                $this->cfg->setNested("lobby.y", $b->y);
                $this->cfg->setNested("lobby.z", $b->z);
                $this->cfg->setNested("lobby.world", $b->level->getName());
                $p->sendMessage($this->getPrefix().$this->getMsg('mainlobby'));
                unset($this->setters[strtolower($p->getName())]['type']);
                return;
            }
        }
    }
    //for Freezecraft only
    /*public function onBlockPlace(BlockPlaceEvent $e){
        if(!$e->getPlayer()->isOp()){
            $e->setCancelled(true);
        }
    }
    //for freezecraft only
    public function onJoin(PlayerJoinEvent $e){
        $p = $e->getPlayer();
        $p->teleport($this->getServer()->getDefaultLevel()->getSpawnLocation());
    }*/
    
    public function onChat(PlayerChatEvent $e){
        $p = $e->getPlayer();
        $msg = strtolower(trim($e->getMessage()));
        if(isset($this->setters[strtolower($p->getName())]['arena'])){
            $e->setCancelled(true);
            $arena = new ConfigManager($this->setters[strtolower($p->getName())]['arena'], $this);
            switch($msg){
                case 'joinsign':
                    $this->setters[strtolower($p->getName())]['type'] = 'setjoinsign';
                    $p->sendMessage($this->getPrefix().$this->getMsg('break_sign'));
                    return;
                case 'returnsign':
                    $this->setters[strtolower($p->getName())]['type'] = 'setreturnsign';
                    $p->sendMessage($this->getPrefix().$this->getMsg('break_sign'));
                    return;
                case 'startpos':
                    $this->setters[strtolower($p->getName())]['type'] = 'setjoinpos';
                    $p->sendMessage($this->getPrefix().$this->getMsg('break_block'));
                    return;
                case 'lobbypos':
                    $this->setters[strtolower($p->getName())]['type'] = 'setlobbypos';
                    $p->sendMessage($this->getPrefix().$this->getMsg('break_block'));
                    return;
                case 'corners':
                    $this->setters[strtolower($p->getName())]['type'] = 'setfirstcorner';
                    $p->sendMessage($this->getPrefix().$this->getMsg('break_block'));
                    return;
                case 'spectatorspawn':
                    $this->setters[strtolower($p->getName())]['type'] = 'setspecspawn';
                    $p->sendMessage($this->getPrefix().$this->getMsg('break_block'));
                    return;
                case 'leavepos':
                    $this->setters[strtolower($p->getName())]['type'] = 'setleavepos';
                    $p->sendMessage($this->getPrefix().$this->getMsg('break_block'));
                    return;
                case 'done':
                    $p->sendMessage($this->getPrefix().$this->getMsg('disable_setup_mode'));
                    $this->reloadArena($this->setters[strtolower($p->getName())]['arena']);
                    unset($this->setters[strtolower($p->getName())]);
                    return;
            }
            $args = explode(' ', $msg);
            if(count($args) >= 1 && count($args) <= 2){
                if($args[0] === 'help'){
                    $help1 = $this->getMsg('help_joinsign')
                            . $this->getMsg('help_returnsign')
                            . $this->getMsg('help_startpos')
                            . $this->getMsg('help_lobbypos')
                            . $this->getMsg('help_corners')
                            . $this->getMsg('help_spectatorspawn')
                            . $this->getMsg('help_leavepos');
                    $help2 = $this->getMsg('help_time')
                            . $this->getMsg('help_colortime')
                            . $this->getMsg('help_type')
                            . $this->getMsg('help_material')
                            . $this->getMsg('help_allowstatus')
                            . $this->getMsg('help_world')
                            . $this->getMsg('help_statusline');
                    $help3 = $this->getMsg('help_allowspectator')
                            . $this->getMsg('help_signupdatetime')
                            . $this->getMsg('help_maxtime')
                            . $this->getMsg('help_maxplayers')
                            . $this->getMsg('help_minplayers');
                    $helparray = [$help1, $help2, $help3];
                    if(isset($args[1])){
                        if(intval($args[1]) >= 1 && intval($args[1]) <= 3){
                            $help = "§9--- §6§lColorMatch setup help§l $args[1]/3§9 ---§r§f";
                            $help .= $helparray[intval(intval($args[1]) - 1)];
                            $p->sendMessage($help);
                            return;
                        }
                        $p->sendMessage($this->getPrefix()."§6use: §ahelp §b[page 1-3]");
                        return;
                    }
                    $p->sendMessage("§9--- §6§lColorMatch setup help§l 1/3§9 ---§r§f".$help1);
                    return;
                }
            }
            if(count(explode(' ', $msg)) !== 2 && strpos($msg, 'statusline') !== 0){
                $p->sendMessage($this->getPrefix().$this->getMsg('invalid_arguments'));
                return;
            }
            if(substr($msg, 0, 10) === 'statusline'){
                if(!strlen(substr($msg, 13)) >= 1 || !intval(substr($msg, 11, 1)) >= 1 || !intval(substr($msg, 11, 1) <= 4)){
                    $p->sendMessage($this->getPrefix().$this->getMsg('statusline_help'));
                    return;
                }
                $arena->setStatusLine($args[1], substr($msg, 13));
                $p->sendMessage($this->getPrefix().$this->getMsg('statusline'));
                return;
            }
            elseif(strpos($msg, 'type') === 0){
                if(substr($msg, 5) === 'classic' || substr($msg, 5) === 'furious' || substr($msg, 5) === 'stoned'){
                    $arena->setType(substr($msg, 4));
                    $p->sendMessage($this->getPrefix().$this->getMsg('type'));
                    return;
                }
                $p->sendMessage($this->getPrefix().$this->getMsg('type_help'));
                return;
            }
            elseif(strpos($msg, 'enable') === 0){
                if(substr($msg, 7) === 'true' || substr($msg, 7) === 'false'){
                    $arena->setEnable(substr($msg, 7));
                    $p->sendMessage($this->getPrefix().$this->getMsg('enable'));
                    return;
                }
                $p->sendMessage($this->getPrefix().$this->getMsg('enable_help'));
                return;
            }
            elseif(strpos($msg, 'material') === 0){
                if(substr($msg, 9) === 'wool' || substr($msg, 9) === 'clay'){
                    $arena->setMaterial(substr($msg, 9));
                    $p->sendMessage($this->getPrefix().$this->getMsg('material'));
                    return;
                }
                $p->sendMessage($this->getPrefix().$this->getMsg('material_help'));
            }
            elseif(strpos($msg, 'allowstatus') === 0){
                if(substr($msg, 12) === 'true' || substr($msg, 12) === 'false'){
                    $arena->setStatus(substr($msg, 12));
                    $p->sendMessage($this->getPrefix().$this->getMsg('allowstatus'));
                    return;
                }
                $p->sendMessage($this->getPrefix().$this->getMsg('allowstatus_help'));
            }
            elseif(strpos($msg, 'signupdatetime') === 0){
                if(!is_numeric(substr($msg, 15))){
                    $p->sendMessage($this->getPrefix().$this->getMsg('signupdatetime'));
                    return;
                }
                $arena->setUpdateTime(substr($msg, 15));
                $p->sendMessage($this->getPrefix().$this->getMsg('signupdatetime'));
            }
            elseif(strpos($msg, 'world') === 0){
                if(is_string(substr($msg, 6))){
                    $arena->setArenaWorld(substr($msg, 6));
                    $p->sendMessage($this->getPrefix().$this->getMsg('world'));
                    return;
                }
                $p->sendMessage($this->getPrefix().$this->getMsg('world_help'));
            }
            elseif(strpos($msg, 'allowspectator') === 0){
                if(substr($msg, 15) === 'true' || substr($msg, 15) === 'false'){
                    $arena->setSpectator(substr($msg, 15));
                    $p->sendMessage($this->getPrefix().$this->getMsg('allowspectator'));
                    return;
                }
                $p->sendMessage($this->getPrefix().$this->getMsg('allowspectator_help'));
            }
            elseif(strpos($msg, 'maxtime') === 0){
                if(!is_numeric(substr($msg, 8))){
                    $p->sendMessage($this->getPrefix().$this->getMsg('maxtime_help'));
                    return;
                }
                $arena->setMaxTime(substr($msg, 8));
                $p->sendMessage($this->getPrefix().$this->getMsg('maxtime'));
            }
            elseif(strpos($msg, 'maxplayers') === 0){
                if(!is_numeric(substr($msg, 11))){
                    $p->sendMessage($this->getPrefix().$this->getMsg('maxplayers_help'));
                    return;
                }
                $arena->setMaxPlayers(substr($msg, 11));
                $p->sendMessage($this->getPrefix().$this->getMsg('maxplayers'));
            }
            elseif(strpos($msg, 'minplayers') === 0){
                if(!is_numeric(substr($msg, 11))){
                    $p->sendMessage($this->getPrefix().$this->getMsg('minplayers_help'));
                    return;
                }
                $arena->setMinPlayers(substr($msg, 11));
                $p->sendMessage($this->getPrefix().$this->getMsg('minplayers'));
            }
            elseif(strpos($msg, 'starttime') === 0){
                if(!is_numeric(substr($msg, 10))){
                    $p->sendMessage($this->getPrefix().$this->getMsg('starttime_help'));
                    return;
                }
                $arena->setStartTime(substr($msg, 10));
                $p->sendMessage($this->getPrefix().$this->getMsg('starttime'));
            }
            elseif(strpos($msg, 'colortime') === 0){
                if(!is_numeric(substr($msg, 10))){
                    $p->sendMessage($this->getPrefix().$this->getMsg('colortime_help'));
                    return;
                }
                $arena->setColorTime(substr($msg, 10));
                $p->sendMessage($this->getPrefix().$this->getMsg('colortime'));
            }
            elseif(strpos($msg, 'time') === 0){
                if(substr($msg, 5) === 'true' || substr($msg, 5) === 'day' || substr($msg, 5) === 'night' || is_numeric(substr($msg, 5))){
                    $arena->setTime(substr($msg, 5));
                    $p->sendMessage($this->getPrefix().$this->getMsg('time'));
                    return;
                }
                $p->sendMessage($this->getPrefix().$this->getMsg('time_help'));
            }
            else{
                $p->sendMessage($this->getPrefix().$this->getMsg('invalid_arguments'));
            }
        }
    }
    
    public function onQuit(PlayerQuitEvent $e){
        $p = $e->getPlayer();
        //for FC only
        //$p->teleportImmediate($this->getServer()->getDefaultLevel()->getSpawnLocation());
        $this->unsetPlayers($p);
    }
    
    public function onKick(PlayerKickEvent $e){
        $p = $e->getPlayer();
        //for FC only
        //$p->teleportImmediate($this->getServer()->getDefaultLevel()->getSpawnLocation());
        $this->unsetPlayers($p);
    }
    
    public function unsetPlayers(Player $p){
        if(isset($this->selectors[strtolower($p->getName())])){
            unset($this->selectors[strtolower($p->getName())]);
        }
        if(isset($this->setters[strtolower($p->getName())])){
            $this->reloadArena($this->setters[strtolower($p->getName())]['arena']);
            if($this->isArenaSet($this->setters[strtolower($p->getName())]['arena'])){
                $a = new Arena($this->setters[strtolower($p->getName())]['arena'], $this);
                $a->setup = false;
            }
            unset($this->setters[strtolower($p->getName())]);
        }
    }
    
    public function reloadArena($name){
        $arena = new Config($this->getDataFolder()."arenas/$name.yml");
        if(!$this->checkFile($arena) || $arena->get('enabled') === "false"){
            $this->arenas[$name] = $arena->getAll();
            $this->arenas[$name]['enable'] = false;
            return;
        }
        if($this->arenas[$name]['enable'] === false){
            $this->setArenasData($arena, $name);
            return;
        }
        $this->arenas[$name] = $arena->getAll();
        $this->arenas[$name]['enable'] = true;
        $this->ins[$name]->data = $this->arenas[$name];
    }
    
    public function getPlayerArena(Player $p){
        foreach($this->ins as $arena){
            $players = array_merge($arena->ingamep, $arena->lobbyp, $arena->spec);
            if(isset($players[strtolower($p->getName())])){
                return $arena;
            }
        }
        return false;
    }
    
    public function isArenaSet($name){
        if(isset($this->ins[$name])) return true;
        return false;
    }
    
    public function registerEconomy(){
        $economy = ["EconomyAPI", "PocketMoney", "MassiveEconomy", "GoldStd"];
        foreach($economy as $plugin){
            $ins = $this->getServer()->getPluginManager()->getPlugin($plugin);
            if($ins instanceof Plugin && $ins->isEnabled()){
                $this->economy = $ins;
                $this->getServer()->getLogger()->info("Selected economy plugin: $plugin");
                return;
            }
        }
        $this->economy = null;
    }
}