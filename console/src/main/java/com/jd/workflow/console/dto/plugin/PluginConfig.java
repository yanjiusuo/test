package com.jd.workflow.console.dto.plugin;

import com.jd.workflow.soap.common.xml.XNode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 插件配置项目
 */
@Data
public class PluginConfig {
    List<PluginConfigItem> plugins;

    public String toXml(){
        XNode root = XNode.make("plugins");
        if(plugins != null){
            for (PluginConfigItem plugin : plugins) {
                root.makeChild("plugin")
                        .attr("id",plugin.getId())
                        .attr("url",plugin.getUrl())
                        .attr("version",plugin.getVersion())
                        .makeChild("idea-version")
                            .attr("since-build",plugin.sinceBuild)
                            .attr("until-build",plugin.untilBuild);

            }
        }
        return root.toXml();
    }

    public static void main(String[] args) {
        PluginConfig pluginConfig = new PluginConfig();
        pluginConfig.setPlugins(new ArrayList<>());
        {
          PluginConfigItem item = new PluginConfigItem();
          item.setId("localDebugPlugin");
          item.setUrl("1");
          item.setVersion("1.0.0");
          item.setSinceBuild("181.3");
          item.setUntilBuild("191.3");
          pluginConfig.getPlugins().add(item);
        }
        System.out.println(pluginConfig.toXml());
    }
}
