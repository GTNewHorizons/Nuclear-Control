package shedar.mods.ic2.nuclearcontrol.api;

import java.util.List;

public class PanelContent {

    private List<PanelString> lines;

    public PanelContent(List<PanelString> lines) {
        this.lines = lines;
    }

    public List<PanelString> getLines() {
        return lines;
    }
}
