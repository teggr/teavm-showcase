//usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 21+
import java.awt.AlphaComposite;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

class PrepareArtwork {
    public static void main(String[] args) throws Exception {
        BufferedImage reference = ImageIO.read(Path.of(args[0]).toFile());
        Path output = Path.of(args[1]);
        Files.createDirectories(output);
        cut(reference, output.resolve("portrait.png"), 740, 55, 450, 380,
                new int[]{752,777,792,777,767,785,825,881,944,1003,1025,1059,1089,1124,1118,1075,1043,1038,1062,1136,1163},
                new int[]{427,373,328,302,235,175,111,83,67,73,98,113,166,211,232,245,258,309,344,410,427});
        cut(reference, output.resolve("mountains.png"), 1215, 170, 457, 260,
                new int[]{1215,1280,1308,1331,1353,1365,1378,1393,1423,1449,1470,1487,1530,1558,1595,1630,1672,1672,1610,1450,1255},
                new int[]{418,345,305,248,206,179,221,253,298,282,334,347,327,354,391,395,407,394,395,412,425});
        System.out.println("Reference: " + reference.getWidth() + " x " + reference.getHeight());
    }

    private static void cut(BufferedImage source, Path target, int x, int y, int width, int height,
                            int[] xs, int[] ys) throws Exception {
        int[] localX = xs.clone();
        int[] localY = ys.clone();
        for (int i = 0; i < xs.length; i++) {
            localX[i] -= x;
            localY[i] -= y;
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        var graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setClip(new Polygon(localX, localY, xs.length));
        graphics.setComposite(AlphaComposite.Src);
        graphics.drawImage(source, -x, -y, null);
        graphics.dispose();
        ImageIO.write(image, "png", target.toFile());
    }
}
