package org.likeapp.likeapp.service.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class MicReader
  implements Runnable
{
  public static final double MAX_LIMIT = 50.0;

  private static final Logger LOG = LoggerFactory.getLogger (MicReader.class);

  private static final int BUFFER_SIZE = 8192;
  private static final double DIVIDER = BUFFER_SIZE;

  private static final MicReader MIC_READER = new MicReader ();

  private ArrayList<MicReaderListener> listeners = new ArrayList<> (1);
  private boolean isReading;
  private double limit = MAX_LIMIT - 10.0;
  private double value;

  private void MicReader ()
  {
  }

  public static void addListener (MicReaderListener l)
  {
    MIC_READER.listeners.add (l);
  }

  public static void removeListener (MicReaderListener l)
  {
    MIC_READER.listeners.remove (l);
  }

  public static synchronized void start ()
  {
    if (!MIC_READER.isReading)
    {
      LOG.debug ("mic read start");
      MIC_READER.isReading = true;
      new Thread (MIC_READER).start ();
    }
  }

  public static synchronized void stop ()
  {
    LOG.debug ("mic read stop");
    MIC_READER.isReading = false;
  }

  public static void setLimit (double limit)
  {
    LOG.debug ("mic set limit " + limit);
    MIC_READER.limit = limit;
  }

  public static double getValue ()
  {
    return MIC_READER.value;
  }

  public static double getLimit ()
  {
    return MIC_READER.limit;
  }

  public static boolean isEnabled ()
  {
    boolean enabled = false;
    try
    {
      AudioRecord audioRecord = MIC_READER.getAudioRecord ();
      enabled = audioRecord.getState () == AudioRecord.STATE_INITIALIZED;
    }
    catch (Exception ignored)
    {
    }

    return enabled;
  }

  public static boolean isRunning ()
  {
    return MIC_READER.isReading;
  }

  private AudioRecord getAudioRecord ()
  {
    int sampleRate = 8000;
    int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    int minInternalBufferSize = AudioRecord.getMinBufferSize (sampleRate, channelConfig, audioFormat);
    int internalBufferSize = minInternalBufferSize * 4;

    return new AudioRecord (MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, internalBufferSize);
  }

  @Override
  public void run ()
  {
    AudioRecord audioRecord = getAudioRecord ();
    try
    {
      audioRecord.startRecording ();
    }
    catch (java.lang.IllegalStateException ignored)
    {
      return;
    }

    byte[] buffer = new byte[BUFFER_SIZE];

    while (isReading)
    {
      int readCount = audioRecord.read (buffer, 0, buffer.length);
      if (readCount > buffer.length / 2)
      {
        double sum = 0.0;
        for (int i = 0; i < readCount; )
        {
          int b = (buffer[i++] & 0xff) | (buffer[i++] << 8);
          double current_sample = b >= 0 ? 1.0 * b : -1.0 * b;
          sum += 10.0 * Math.log10 ((current_sample + 1.0 / DIVIDER) / DIVIDER);
        }

        sum /= readCount;
        sum += MAX_LIMIT;

        for (MicReaderListener l : listeners)
        {
          l.micUpdate (sum);
        }

        value = sum;

        if (sum >= limit)
        {
          for (MicReaderListener l : listeners)
          {
            l.micLimitExceeded (sum);
          }
        }
      }
      else if (readCount == 0)
      {
        try
        {
          Thread.sleep (1000);
        }
        catch (InterruptedException ignored)
        {
        }
      }
      else
      {
        break;
      }
    }

    audioRecord.stop ();
  }
}
