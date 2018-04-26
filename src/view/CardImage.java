package view;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class CardImage {
    private static int cardWidth;
    private static int cardHeight;
    private static int offset;

    static {
        Properties properties = new Properties();
        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream("resources/card-dimension.properties");

            properties.load(inputStream);

            cardWidth = Integer.parseInt(properties.getProperty("card-width"));
            cardHeight = Integer.parseInt(properties.getProperty("card-height"));
            offset = Integer.parseInt(properties.getProperty("optimal-offset"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private CardImage() {}

    static int getCardWidth() {
        return cardWidth;
    }

    static int getCardHeight() {
        return cardHeight;
    }

    static int getOffset() {
        return offset;
    }
}
