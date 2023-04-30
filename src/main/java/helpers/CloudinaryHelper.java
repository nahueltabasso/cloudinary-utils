package helpers;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import utils.CloudinaryUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Component
public class CloudinaryHelper {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryHelper.class);

    @Autowired
    private Cloudinary cloudinary;
    @Value("${cloudinary.cloud_name}")
    private String cloudName;
    @Value("${cloudinary.api_key}")
    public String apiKey;
    @Value("${cloudinary.api_secret}")
    public String apiSecret;
    @Value("${cloudinary.delete_uri}")
    private String deleteUri;

    public String uploadImage(File file) {
        logger.info("Enter to uploadImage()");
        try {
            Map result = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());
            String imageUrl = (String) result.get("secure_url");
            return imageUrl;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String destroyImage(String path) {
        logger.info("Enter to destroyImage()");
        try {
            String publicId = extractPublicIdFromCloudinaryUrl(path);
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpDelete httpDelete = new HttpDelete(deleteUri + "?api_key=" + apiKey +
                    "&public_id=" + publicId +
                    "&timestamp=" + System.currentTimeMillis() / 1000L +
                    "&signature=" + CloudinaryUtils.signatureGenerator(publicId, apiSecret));
            HttpResponse response =  httpClient.execute(httpDelete);

            return String.valueOf(response.getStatusLine().getStatusCode());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String applyImageTransformations(String imageUrl, Map<String, String> transformations) {
        logger.info("Enter to applyImageTransformations()");

        try {
            Transformation transformation = new Transformation();
            for (String key : transformations.keySet()) {
                String value = transformations.get(key);
                switch (key) {
                    case "crop" -> transformation.crop(value);
                    case "width" -> transformation.width(Integer.parseInt(value));
                    case "height" -> transformation.height(Integer.parseInt(value));
                    case "gravity" -> transformation.gravity(value);
                    case "radius" -> transformation.radius(value);

                    default -> throw new IllegalArgumentException("Unknow transformation -> " + key);
                }
            }
            logger.info("Image url before apply transformations: " + imageUrl);
            String publicId = extractPublicIdFromCloudinaryUrl(imageUrl).concat(".jpg");
            String transformationPart = transformation.generate();
            String transformedImageUrl = String.format("https://res.cloudinary.com/%s/image/upload/%s/%s",
                    cloudName, transformationPart, publicId);
            logger.info("Image url after apply transformations: " + transformedImageUrl);
            return transformedImageUrl;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String extractPublicIdFromCloudinaryUrl(String url) {
        String[] parts = url.split("/");
        String fileName = parts[parts.length - 1];
        String publicId = fileName.split("\\.")[0];
        return publicId;
    }
}
