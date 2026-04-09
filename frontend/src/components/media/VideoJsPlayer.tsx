import "@videojs/react/video/skin.css";
import { createPlayer, videoFeatures } from "@videojs/react";
import { VideoSkin, Video } from "@videojs/react/video";

const Player = createPlayer({ features: videoFeatures });

interface VideoJsPlayerProps {
  src: string;
  poster?: string;
}

export function VideoJsPlayer({ src, poster }: VideoJsPlayerProps) {
  return (
    <Player.Provider>
      <VideoSkin>
        <Video key={src} src={src} poster={poster} autoPlay playsInline />
      </VideoSkin>
    </Player.Provider>
  );
}
