import "@videojs/react/video/skin.css";
import { createPlayer, videoFeatures } from "@videojs/react";
import { VideoSkin, Video } from "@videojs/react/video";

const Player = createPlayer({ features: videoFeatures });

interface VideoJsPlayerProps {
  src: string;
  poster?: string;
  startTime?: number;
  onTimeUpdate?: (time: number) => void;
}

export function VideoJsPlayer({ src, poster, startTime, onTimeUpdate }: VideoJsPlayerProps) {
  return (
    <Player.Provider>
      <VideoSkin>
        <Video
          key={src}
          src={src}
          poster={poster}
          autoPlay
          playsInline
          onLoadedMetadata={(event) => {
            if (!startTime) return;
            const target = event.currentTarget;
            if (target && target.currentTime < startTime) {
              target.currentTime = startTime;
            }
          }}
          onTimeUpdate={(event) => {
            if (!onTimeUpdate) return;
            onTimeUpdate(event.currentTarget.currentTime);
          }}
        />
      </VideoSkin>
    </Player.Provider>
  );
}
