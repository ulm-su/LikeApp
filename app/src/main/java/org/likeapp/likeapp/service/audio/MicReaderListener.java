package org.likeapp.likeapp.service.audio;

public interface MicReaderListener
{
  public void micLimitExceeded (double value);

  public void micUpdate (double value);
}

