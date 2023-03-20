package com.pingidentity.sdk.pingonewallet.sample.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.caverock.androidsvg.SVG;
import com.pingidentity.did.sdk.types.Claim;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BitmapUtil {

    private static final List<String> imageKeys = Arrays.asList("selfie", "cardimage", "frontimage", "backimage");

    private BitmapUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static List<String> getImageKeys(){
        return imageKeys;
    }

    public static Bitmap convertSvgToBitmap(@NonNull final String svgString, final int imageWidth) {
        try {
            SVG svg = SVG.getFromString(svgString.replace("image xlink:href", "image href"));
            float svgAspectRatio = svg.getDocumentAspectRatio();
            int bitmapWidth = imageWidth == -1 ? 720 : imageWidth;
            int bitmapHeight = (int) (bitmapWidth / svgAspectRatio);
            Bitmap svgBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas svgCanvas = new Canvas(svgBitmap);
            svgCanvas.drawRGB(255, 255, 255);
            svg.renderToCanvas(svgCanvas);
            return svgBitmap;
        } catch (Exception e) {
            Log.e("TAG", "Failed to parse svg from String", e);
            return null;
        }
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap getBitmapFromClaim(Claim claim) {
        Optional<String> base64Image = claim.getData().entrySet().stream()
                .filter(entry -> BitmapUtil.imageKeys.contains(entry.getKey().toLowerCase()) && !entry.getValue().isEmpty())
                .findFirst()
                .map(Map.Entry::getValue);

        if (!base64Image.isPresent()) {
            return null;
        }

        final Bitmap svgImage = BitmapUtil.convertSvgToBitmap(base64Image.get(), 500);
        if (svgImage != null) {
            return svgImage;
        }

        final byte[] decodedImage = Base64.decode(base64Image.get(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.length);
    }

}
