package ColorMatch.Data;

import ColorMatch.Arena.Configuration;
import lombok.Getter;
import lombok.Setter;

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
