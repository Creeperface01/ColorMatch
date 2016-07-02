package ColorMatch.Data;

import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.AxisAlignedBB;
import lombok.Getter;
import lombok.Setter;
import ColorMatch.Arena.Configuration;

public class SetupMode {

    @Getter
    private boolean created = false;

    //
    @Getter
    @Setter
    protected boolean joinSign = false;

    @Getter
    @Setter
    protected boolean startPos = false;

    @Getter
    @Setter
    protected boolean spectatorPos = false;

    @Getter
    @Setter
    protected boolean floorPos = false;

    @Getter
    @Setter
    protected boolean floor = false;

    @Getter
    @Setter
    protected boolean radius = false;

    @Getter
    @Setter
    protected boolean type = false;

    @Getter
    @Setter
    protected boolean colorChangeInterval = false;

    @Getter
    @Setter
    protected boolean floorMaterial = false;
    protected boolean floorType = false;

    @Getter
    @Setter
    protected boolean level = false;
    //

    Configuration cfg;

    public SetupMode(Configuration cfg, boolean created) {
        this.cfg = cfg;
        this.created = created;
    }


}
