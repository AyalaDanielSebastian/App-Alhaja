import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Diagrama {

    // Escapes Unicode para no depender de la codificacion del archivo fuente.
    static final String PUNTO = "\u00b7";
    static final String CATALOGO = "Cat\u00e1logo";
    static final String JOYERIA = "joyer\u00eda";
    static final String CAMARA = "C\u00e1mara";

    static final Color MORADO = new Color(0x68, 0x42, 0x74);
    static final Color MORADO_TEXTO = new Color(0x3B, 0x24, 0x47);
    static final Color MORADO_SUAVE = new Color(0xF3, 0xEC, 0xF7);
    static final Color MORADO_MEDIO = new Color(0xED, 0xE3, 0xF3);
    static final Color ORO = new Color(0x8A, 0x6A, 0x1E);
    static final Color ORO_TEXTO = new Color(0x4A, 0x3A, 0x12);
    static final Color ORO_SUAVE = new Color(0xFA, 0xF3, 0xE2);
    static final Color GRIS = new Color(0x5B, 0x5B, 0x66);
    static final Color GRIS_SUAVE = new Color(0xF1, 0xF0, 0xF4);
    static final Color GRIS_TEXTO = new Color(0x2C, 0x2C, 0x36);
    static final Color NOTA = new Color(0x6B, 0x5A, 0x75);

    static Graphics2D g;

    public static void main(String[] args) throws Exception {
        int ancho = 1040;
        int alto = 700;
        double escala = 2.0;

        BufferedImage imagen = new BufferedImage(
                (int) (ancho * escala), (int) (alto * escala), BufferedImage.TYPE_INT_RGB);
        g = imagen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, imagen.getWidth(), imagen.getHeight());
        g.setTransform(AffineTransform.getScaleInstance(escala, escala));

        centrado("Alhaja " + PUNTO + " Arquitectura en capas (MVVM + Repositorio)",
                520, 38, 23, Font.BOLD, MORADO_TEXTO);

        // Capa UI
        banda(30, 58, 980, 92, MORADO_SUAVE, MORADO);
        etiqueta("UI " + PUNTO + " Jetpack Compose + Navigation", 46, 80, MORADO);
        String[] pantallas = {CATALOGO, "Detalle", "Favoritas", "Lugares", "Ajustes"};
        for (int i = 0; i < pantallas.length; i++) {
            int x = 46 + i * 195;
            caja(x, 92, 183, 44, MORADO);
            centrado(pantallas[i], x + 91, 120, 14, Font.PLAIN, MORADO_TEXTO);
        }

        flecha(520, 150, 520, 174);
        nota("observa StateFlow", 536, 167);

        // Capa ViewModel
        banda(30, 176, 980, 80, MORADO_MEDIO, MORADO);
        etiqueta("ViewModel " + PUNTO + " AlhajaViewModel", 46, 198, MORADO);
        caja(46, 208, 963, 38, MORADO);
        centrado("StateFlow " + PUNTO + " viewModelScope.launch " + PUNTO + " funciones suspend",
                527, 233, 14, Font.PLAIN, MORADO_TEXTO);

        flecha(520, 256, 520, 280);
        nota("depende solo de interfaces", 536, 273);

        // Capa Domain
        banda(30, 282, 980, 92, ORO_SUAVE, ORO);
        etiqueta("Domain " + PUNTO + " modelos e interfaces de repositorio", 46, 304, ORO);
        String[] interfaces = {"JoyasRepository", "LugaresRepository", "PreferenciasRepository"};
        for (int i = 0; i < interfaces.length; i++) {
            int x = 46 + i * 325;
            caja(x, 316, 313, 44, ORO);
            centrado(interfaces[i], x + 156, 344, 14, Font.PLAIN, ORO_TEXTO);
        }

        flecha(520, 374, 520, 398);
        nota("implementado por", 536, 391);

        // Capa Data
        banda(30, 400, 980, 92, MORADO_SUAVE, MORADO);
        etiqueta("Data " + PUNTO + " implementaciones del repositorio", 46, 422, MORADO);
        String[] impls = {"JoyasRepositoryImpl", "LugaresRepositoryImpl", "PreferenciasRepositoryImpl"};
        for (int i = 0; i < impls.length; i++) {
            int x = 46 + i * 325;
            caja(x, 434, 313, 44, MORADO);
            centrado(impls[i], x + 156, 462, 14, Font.PLAIN, MORADO_TEXTO);
        }

        flecha(202, 492, 202, 526);
        flecha(527, 492, 527, 526);
        flecha(852, 492, 852, 526);

        // Fuentes de datos
        banda(30, 528, 980, 110, GRIS_SUAVE, GRIS);
        etiqueta("Fuentes de datos", 46, 550, GRIS_TEXTO);
        String[][] fuentes = {
            {"Room (SQLite)", "Favoritos " + PUNTO + " Lugares " + PUNTO + " Fotos"},
            {"Retrofit + Gson", "Fake Store API (" + JOYERIA + ")"},
            {"DataStore", "Modo oscuro " + PUNTO + " Moneda"},
            {"Hardware", "GPS " + PUNTO + " " + CAMARA + " (FileProvider)"}
        };
        for (int i = 0; i < fuentes.length; i++) {
            int x = 46 + i * 243;
            caja(x, 562, 233, 60, GRIS);
            centrado(fuentes[i][0], x + 116, 587, 14, Font.BOLD, GRIS_TEXTO);
            centrado(fuentes[i][1], x + 116, 607, 12, Font.PLAIN, GRIS);
        }

        centrado("El ViewModel nunca accede a Retrofit ni a los DAO de Room: siempre pasa por una interfaz de repositorio del dominio.",
                520, 670, 13, Font.PLAIN, GRIS);

        g.dispose();
        File salida = new File("docs/arquitectura.png");
        ImageIO.write(imagen, "png", salida);
        System.out.println("PNG generado: " + salida.getAbsolutePath()
                + " (" + imagen.getWidth() + "x" + imagen.getHeight() + ")");
    }

    static void banda(int x, int y, int w, int h, Color relleno, Color borde) {
        RoundRectangle2D r = new RoundRectangle2D.Double(x, y, w, h, 14, 14);
        g.setColor(relleno);
        g.fill(r);
        g.setColor(borde);
        g.setStroke(new BasicStroke(1.5f));
        g.draw(r);
    }

    static void caja(int x, int y, int w, int h, Color borde) {
        RoundRectangle2D r = new RoundRectangle2D.Double(x, y, w, h, 10, 10);
        g.setColor(Color.WHITE);
        g.fill(r);
        g.setColor(borde);
        g.setStroke(new BasicStroke(1.2f));
        g.draw(r);
    }

    static void etiqueta(String texto, int x, int y, Color color) {
        g.setColor(color);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.drawString(texto, x, y);
    }

    static void nota(String texto, int x, int y) {
        g.setColor(NOTA);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.drawString(texto, x, y);
    }

    static void centrado(String texto, int cx, int y, int tamano, int estilo, Color color) {
        g.setColor(color);
        g.setFont(new Font(Font.SANS_SERIF, estilo, tamano));
        int w = g.getFontMetrics().stringWidth(texto);
        g.drawString(texto, cx - w / 2, y);
    }

    static void flecha(int x1, int y1, int x2, int y2) {
        g.setColor(MORADO);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(x1, y1, x2, y2 - 8);
        Path2D punta = new Path2D.Double();
        punta.moveTo(x2, y2);
        punta.lineTo(x2 - 5, y2 - 9);
        punta.lineTo(x2 + 5, y2 - 9);
        punta.closePath();
        g.fill(punta);
    }
}
