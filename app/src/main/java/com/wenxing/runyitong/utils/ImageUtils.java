package com.wenxing.runyitong.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageUtils {
    
    private static final String TAG = "ImageUtils";
    private static final int MAX_IMAGE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1080;
    private static final int JPEG_QUALITY = 85;
    private static final int THUMBNAIL_SIZE = 200;
    
    // 线程池用于异步图片处理
    private static final ExecutorService imageProcessorExecutor = Executors.newFixedThreadPool(2);
    
    // 图片编辑操作类型
    public enum EditOperation {
        ROTATE_90_CW,    // 顺时针旋转90度
        ROTATE_90_CCW,   // 逆时针旋转90度
        FLIP_HORIZONTAL, // 水平翻转
        FLIP_VERTICAL,   // 垂直翻转
        CROP,            // 裁剪
        BRIGHTNESS,      // 亮度调整
        CONTRAST         // 对比度调整
    }
    
    // 图片处理回调接口
    public interface ImageProcessCallback {
        void onSuccess(Bitmap result);
        void onError(String error);
    }
    
    /**
     * 将Uri转换为MultipartBody.Part
     * @param context 上下文
     * @param imageUri 图片Uri
     * @param partName 参数名称
     * @return MultipartBody.Part
     */
    public static MultipartBody.Part createImagePart(Context context, Uri imageUri, String partName) {
        try {
            // 压缩图片
            Bitmap compressedBitmap = compressImage(context, imageUri);
            
            // 创建临时文件
            File tempFile = createTempImageFile(context, compressedBitmap);
            
            // 创建RequestBody
            RequestBody requestBody = RequestBody.create(
                MediaType.parse("image/jpeg"), 
                tempFile
            );
            
            // 创建MultipartBody.Part
            return MultipartBody.Part.createFormData(
                partName, 
                tempFile.getName(), 
                requestBody
            );
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 压缩图片
     * @param context 上下文
     * @param imageUri 图片Uri
     * @return 压缩后的Bitmap
     */
    private static Bitmap compressImage(Context context, Uri imageUri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        
        // 获取图片尺寸
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();
        
        // 计算缩放比例
        int inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);
        
        // 重新打开输入流
        inputStream = context.getContentResolver().openInputStream(imageUri);
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;
        
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();
        
        return bitmap;
    }
    
    /**
     * 计算图片缩放比例
     * @param options BitmapFactory.Options
     * @param reqWidth 目标宽度
     * @param reqHeight 目标高度
     * @return 缩放比例
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        return inSampleSize;
    }
    
    /**
     * 创建临时图片文件
     * @param context 上下文
     * @param bitmap 图片Bitmap
     * @return 临时文件
     */
    private static File createTempImageFile(Context context, Bitmap bitmap) throws IOException {
        // 创建临时文件
        File tempFile = File.createTempFile(
            "temp_image_" + System.currentTimeMillis(), 
            ".jpg", 
            context.getCacheDir()
        );
        
        // 将Bitmap保存到文件
        FileOutputStream fos = new FileOutputStream(tempFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
        fos.flush();
        fos.close();
        
        return tempFile;
    }
    
    /**
     * 获取图片文件大小
     * @param context 上下文
     * @param imageUri 图片Uri
     * @return 文件大小（字节）
     */
    public static long getImageSize(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            long size = inputStream.available();
            inputStream.close();
            return size;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 检查图片是否过大
     * @param context 上下文
     * @param imageUri 图片Uri
     * @return 是否过大
     */
    public static boolean isImageTooLarge(Context context, Uri imageUri) {
        return getImageSize(context, imageUri) > MAX_IMAGE_SIZE;
    }
    
    /**
     * 格式化文件大小
     * @param size 文件大小（字节）
     * @return 格式化后的大小字符串
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
    }
    
    /**
     * 异步编辑图片
     * @param context 上下文
     * @param imageUri 图片Uri
     * @param operation 编辑操作
     * @param callback 回调接口
     */
    public static void editImageAsync(Context context, Uri imageUri, EditOperation operation, ImageProcessCallback callback) {
        imageProcessorExecutor.execute(() -> {
            try {
                Bitmap originalBitmap = loadBitmapFromUri(context, imageUri);
                if (originalBitmap == null) {
                    callback.onError("无法加载图片");
                    return;
                }
                
                Bitmap editedBitmap = applyEditOperation(originalBitmap, operation);
                callback.onSuccess(editedBitmap);
                
            } catch (Exception e) {
                Log.e(TAG, "图片编辑失败", e);
                callback.onError("图片编辑失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 应用编辑操作
     * @param bitmap 原始图片
     * @param operation 编辑操作
     * @return 编辑后的图片
     */
    private static Bitmap applyEditOperation(Bitmap bitmap, EditOperation operation) {
        Matrix matrix = new Matrix();
        
        switch (operation) {
            case ROTATE_90_CW:
                matrix.postRotate(90);
                break;
            case ROTATE_90_CCW:
                matrix.postRotate(-90);
                break;
            case FLIP_HORIZONTAL:
                matrix.postScale(-1, 1);
                break;
            case FLIP_VERTICAL:
                matrix.postScale(1, -1);
                break;
            default:
                return bitmap;
        }
        
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
    
    /**
     * 从Uri加载Bitmap
     * @param context 上下文
     * @param imageUri 图片Uri
     * @return Bitmap对象
     */
    private static Bitmap loadBitmapFromUri(Context context, Uri imageUri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        
        // 获取图片尺寸
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();
        
        // 计算缩放比例
        int inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);
        
        // 重新打开输入流并加载图片
        inputStream = context.getContentResolver().openInputStream(imageUri);
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;
        
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();
        
        return bitmap;
    }
    
    /**
     * 生成图片缩略图
     * @param context 上下文
     * @param imageUri 图片Uri
     * @return 缩略图Bitmap
     */
    public static Bitmap generateThumbnail(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            
            // 计算缩略图缩放比例
            int inSampleSize = calculateInSampleSize(options, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
            
            inputStream = context.getContentResolver().openInputStream(imageUri);
            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;
            
            Bitmap thumbnail = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            
            return thumbnail;
            
        } catch (IOException e) {
            Log.e(TAG, "生成缩略图失败", e);
            return null;
        }
    }
    
    /**
     * 保存Bitmap到Uri
     * @param context 上下文
     * @param bitmap 要保存的Bitmap
     * @param filename 文件名
     * @return 保存后的Uri
     */
    public static Uri saveBitmapToUri(Context context, Bitmap bitmap, String filename) {
        try {
            File tempFile = new File(context.getCacheDir(), filename + ".jpg");
            FileOutputStream fos = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
            fos.flush();
            fos.close();
            
            return Uri.fromFile(tempFile);
            
        } catch (IOException e) {
            Log.e(TAG, "保存图片失败", e);
            return null;
        }
    }
    
    /**
     * 清理缓存目录中的临时图片文件
     * @param context 上下文
     */
    public static void cleanupTempFiles(Context context) {
        imageProcessorExecutor.execute(() -> {
            try {
                File cacheDir = context.getCacheDir();
                File[] files = cacheDir.listFiles((dir, name) -> 
                    name.startsWith("temp_image_") && name.endsWith(".jpg"));
                
                if (files != null) {
                    long currentTime = System.currentTimeMillis();
                    for (File file : files) {
                        // 删除超过1小时的临时文件
                        if (currentTime - file.lastModified() > 3600000) {
                            if (file.delete()) {
                                Log.d(TAG, "删除临时文件: " + file.getName());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "清理临时文件失败", e);
            }
        });
    }
    
    /**
     * 获取图片的详细信息
     * @param context 上下文
     * @param imageUri 图片Uri
     * @return 图片信息字符串
     */
    public static String getImageInfo(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            
            long fileSize = getImageSize(context, imageUri);
            String fileSizeStr = formatFileSize(fileSize);
            
            return String.format("尺寸: %dx%d" + System.lineSeparator() + "大小: %s" + System.lineSeparator() + "格式: %s", 
                options.outWidth, options.outHeight, fileSizeStr, options.outMimeType);
                
        } catch (IOException e) {
            Log.e(TAG, "获取图片信息失败", e);
            return "无法获取图片信息";
        }
    }
    
    /**
     * 检查图片是否有效
     * @param context 上下文
     * @param imageUri 图片Uri
     * @return 是否有效
     */
    public static boolean isValidImage(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            
            return options.outWidth > 0 && options.outHeight > 0;
            
        } catch (Exception e) {
            Log.e(TAG, "检查图片有效性失败", e);
            return false;
        }
    }
}