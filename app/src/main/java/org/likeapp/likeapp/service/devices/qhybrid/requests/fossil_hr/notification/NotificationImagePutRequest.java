package org.likeapp.likeapp.service.devices.qhybrid.requests.fossil_hr.notification;

import org.likeapp.likeapp.service.devices.qhybrid.adapter.fossil.FossilWatchAdapter;
import org.likeapp.likeapp.service.devices.qhybrid.requests.fossil_hr.file.AssetFile;
import org.likeapp.likeapp.service.devices.qhybrid.requests.fossil_hr.file.AssetFilePutRequest;
import org.likeapp.likeapp.util.StringUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

public class NotificationImagePutRequest extends AssetFilePutRequest
{
    private NotificationImagePutRequest(String packageName, AssetFile file, FossilWatchAdapter adapter) throws IOException {
        super(file, (byte) 0x01, adapter);
    }

    private NotificationImagePutRequest(NotificationImage image, FossilWatchAdapter adapter) throws IOException {
        super(image, (byte) 0x01, adapter);
    }

    public NotificationImagePutRequest(NotificationImage[] images, FossilWatchAdapter adapter) throws IOException {
        super(images, (byte) 0x01, adapter);
    }


    private static byte[][] prepareFileCrc(String[] packageNames){
        byte[][] names = new byte[packageNames.length][];
        for (int i = 0; i < packageNames.length; i++){
            names[i] = prepareFileCrc(packageNames[i]);
        }
        return names;
    }

    private static byte[] prepareFileCrc(String packageName){
        CRC32 crc = new CRC32();
        crc.update(packageName.getBytes());

        String crcString = StringUtils.bytesToHex(
                ByteBuffer
                        .allocate(4)
                        .order(ByteOrder.LITTLE_ENDIAN)
                        .putInt((int) crc.getValue())
                        .array()
        );

        ByteBuffer buffer = ByteBuffer.allocate(crcString.length() + 1)
                .put(crcString.getBytes())
                .put((byte) 0x00);

        return buffer.array();
    }
}
