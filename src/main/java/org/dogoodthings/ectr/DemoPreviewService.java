package org.dogoodthings.ectr;

import com.dscsag.plm.spi.interfaces.ECTRService;
import com.dscsag.plm.spi.interfaces.objects.ObjectIdentifier;
import com.dscsag.plm.spi.interfaces.services.ui.ObjectLastModifiedTimeData;
import com.dscsag.plm.spi.interfaces.services.ui.ObjectPreviewData;
import com.dscsag.plm.spi.interfaces.services.ui.PreviewService;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemoPreviewService implements PreviewService {
  private final boolean useCache = false;
  private final Map<ObjectIdentifier, ObjectPreviewData> cache = new HashMap<>();
  private final ObjectPreviewData invalid = new ObjectPreviewData(null, null, Instant.ofEpochSecond(100));
  private final ECTRService ectrService;
  private final Font font;

  public DemoPreviewService(ECTRService ectrService) {
    this.ectrService = ectrService;
    font = UIManager.getDefaults().getFont("Menu.font").deriveFont(96.0f);
    log("DemoPreviewService()");
  }

  @Override
  public boolean canHandle(ObjectIdentifier objectIdentifier) {
    //nodes with unknown CAD files in ASV
    return "INSOB".equals(objectIdentifier.getType());
  }

  @Override
  public List<ObjectPreviewData> getPreviews(List<ObjectIdentifier> list) {
    try {
      log("getPreviews");
      var x = list.stream().map(this::getSinglePreview).toList();
      log("getPreviews: " + x.size());
      return x;
    } catch (RuntimeException re) {
      re.printStackTrace();
    }
    return Collections.emptyList();
  }

  @Override
  public List<ObjectLastModifiedTimeData> getLastModifiedTime(List<ObjectIdentifier> list) {
    return list.stream().map(i -> new ObjectLastModifiedTimeData(i, cache.getOrDefault(i, invalid).getLastModifiedTime())).toList();
  }

  private ObjectPreviewData getSinglePreview(ObjectIdentifier i) {
    if (useCache) {
      synchronized (cache) {
        return cache.computeIfAbsent(i, k -> generatePreviewData(k));
      }
    } else
      return generatePreviewData(i);
  }

  private ObjectPreviewData generatePreviewData(ObjectIdentifier oi) {
    int hc = Math.abs(oi.getKey().hashCode());
    int modulo = hc % 256;
    Color bgColor = new Color(hc % 256, ((hc / 256) % 256), (hc / (256 * 256) % 256));

    BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
    var g = image.getGraphics();
    g.setColor(bgColor);
    g.fillRect(0, 0, 300, 300);
    g.setColor(Color.BLUE);
    g.draw3DRect(10, 10, 280, 280, true);
    g.setColor(Color.BLACK);
    g.setFont(font);
    g.drawString("" + modulo, 50, 200);
    return new ObjectPreviewData(oi, List.of(image), Instant.now());
  }

  private void log(Object message) {
    ectrService.getPlmLogger().debug("DEMO: " + message);
  }
}
