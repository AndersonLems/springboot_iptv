import { Dialog, DialogContent, DialogTitle } from "@/components/ui/dialog";
import type { MediaItem } from "@/types/media";
import { useEffect, useMemo, useState } from "react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { VideoJsPlayer } from "@/components/media/VideoJsPlayer";
import { Button } from "@/components/ui/button";

interface PlayerModalProps {
  item: MediaItem | null;
  open: boolean;
  onClose: () => void;
}

function getStreamScore(stream: {
  name?: string;
  quality?: string;
  groupTitle?: string;
}) {
  const text =
    `${stream.name ?? ""} ${stream.quality ?? ""} ${stream.groupTitle ?? ""}`.toLowerCase();

  if (text.includes("4k") || text.includes("uhd")) return 100;
  if (text.includes("1080") || text.includes("fhd") || text.includes("full hd"))
    return 80;
  if (text.includes("hdr")) return 70;
  if (text.includes("720") || text.includes("hd")) return 50;
  return 10;
}

function getBestDefaultStreamUrl(
  streams: Array<{
    streamUrl: string;
    name?: string;
    quality?: string;
    groupTitle?: string;
  }>,
) {
  if (!streams.length) return "";
  return [...streams].sort((a, b) => getStreamScore(b) - getStreamScore(a))[0]
    .streamUrl;
}

export function PlayerModal({ item, open, onClose }: PlayerModalProps) {
  const [selectedStreamUrl, setSelectedStreamUrl] = useState<string>("");
  const [selectedSeason, setSelectedSeason] = useState<number | null>(null);
  const [selectedEpisode, setSelectedEpisode] = useState<number | null>(null);
  const [selectedAudio, setSelectedAudio] = useState<
    "legendado" | "dublado" | null
  >(null);
  const [resumeTime, setResumeTime] = useState<number>(0);

  const streamOptions = useMemo(() => {
    if (!item) return [];
    if (item.streamOptions?.length) return item.streamOptions;
    return [
      {
        name: "Padrão",
        streamUrl: item.streamUrl,
      },
    ];
  }, [item]);

  const seriesStreams = useMemo(() => {
    if (!item || item.type !== "series") return [];
    return streamOptions.filter(
      (stream) =>
        typeof stream.season === "number" && typeof stream.episode === "number",
    );
  }, [item, streamOptions]);

  const episodesBySeason = useMemo(() => {
    const bySeason = new Map<
      number,
      Map<number, { legendado?: (typeof seriesStreams)[number]; dublado?: (typeof seriesStreams)[number] }>
    >();

    const getAudioTag = (stream: (typeof seriesStreams)[number]) => {
      const text =
        `${stream.name ?? ""} ${stream.quality ?? ""} ${stream.groupTitle ?? ""}`.toLowerCase();
      if (text.includes("[l]") || text.includes("legendad")) return "legendado";
      if (text.includes("dublad")) return "dublado";
      return "dublado";
    };

    seriesStreams.forEach((stream) => {
      const season = stream.season;
      const episode = stream.episode;
      if (typeof season !== "number" || typeof episode !== "number") return;
      const audio = getAudioTag(stream);
      if (!audio) return;

      const seasonMap = bySeason.get(season) ?? new Map();
      const current = seasonMap.get(episode) ?? {};
      const existing = current[audio];
      if (!existing || getStreamScore(stream) > getStreamScore(existing)) {
        current[audio] = stream;
      }
      seasonMap.set(episode, current);
      bySeason.set(season, seasonMap);
    });

    return bySeason;
  }, [seriesStreams]);

  const seasons = useMemo(
    () => [...episodesBySeason.keys()].sort((a, b) => a - b),
    [episodesBySeason],
  );

  const episodesForSeason = useMemo(() => {
    const seasonMap = episodesBySeason.get(selectedSeason ?? -1);
    if (!seasonMap) return [] as number[];
    return [...seasonMap.keys()].sort((a, b) => a - b);
  }, [episodesBySeason, selectedSeason]);

  const getStreamForEpisode = (
    season: number,
    episode: number,
    preferred: "legendado" | "dublado" | null,
  ) => {
    const episodeEntry = episodesBySeason.get(season)?.get(episode);
    if (!episodeEntry) return null;
    if (preferred && episodeEntry[preferred]) return episodeEntry[preferred] ?? null;
    return episodeEntry.dublado ?? episodeEntry.legendado ?? null;
  };

  const playbackKey = useMemo(() => {
    if (!item) return null;
    if (
      item.type === "series" &&
      selectedSeason != null &&
      selectedEpisode != null
    ) {
      return `playback:${item.id}:s${selectedSeason}e${selectedEpisode}`;
    }
    return `playback:${item.id}`;
  }, [item, selectedEpisode, selectedSeason]);

  useEffect(() => {
    if (!open || !item) return;
    if (item.type === "series" && seasons.length) {
      const preferredSeason = seasons.includes(1) ? 1 : seasons[0];
      const seasonEpisodes = episodesBySeason.get(preferredSeason);
      const preferredEpisode = seasonEpisodes?.has(1)
        ? 1
        : (seasonEpisodes?.keys() ?? [])[0];
      if (typeof preferredSeason === "number" && typeof preferredEpisode === "number") {
        const entry = episodesBySeason.get(preferredSeason)?.get(preferredEpisode) ?? {};
        const defaultAudio = entry.dublado
          ? "dublado"
          : entry.legendado
          ? "legendado"
          : null;
        setSelectedSeason(preferredSeason);
        setSelectedEpisode(preferredEpisode);
        setSelectedAudio(defaultAudio);
        const stream = getStreamForEpisode(preferredSeason, preferredEpisode, defaultAudio);
        setSelectedStreamUrl(stream?.streamUrl ?? "");
        return;
      }
      return;
    }
    setSelectedSeason(null);
    setSelectedEpisode(null);
    setSelectedAudio(null);
    setSelectedStreamUrl(
      getBestDefaultStreamUrl(streamOptions) || item.streamUrl,
    );
  }, [open, item, streamOptions, seasons, episodesBySeason]);

  useEffect(() => {
    if (!playbackKey) return;
    const raw = localStorage.getItem(playbackKey);
    const parsed = raw ? Number(raw) : 0;
    setResumeTime(Number.isFinite(parsed) ? parsed : 0);
  }, [playbackKey, selectedStreamUrl]);

  useEffect(() => {
    if (!item || item.type !== "series") return;
    if (selectedSeason == null || selectedEpisode == null) return;
    const stream = getStreamForEpisode(
      selectedSeason,
      selectedEpisode,
      selectedAudio,
    );
    if (stream) {
      setSelectedStreamUrl(stream.streamUrl);
    }
  }, [episodesBySeason, item, selectedAudio, selectedEpisode, selectedSeason]);

  const nextEpisode = useMemo(() => {
    if (!item || item.type !== "series") return null;
    if (selectedSeason == null || selectedEpisode == null) return null;
    const currentEpisodes = episodesForSeason;
    const currentIndex = currentEpisodes.indexOf(selectedEpisode);
    if (currentIndex >= 0 && currentIndex + 1 < currentEpisodes.length) {
      const nextEp = currentEpisodes[currentIndex + 1];
      return { season: selectedSeason, episode: nextEp };
    }
    const seasonIndex = seasons.indexOf(selectedSeason);
    if (seasonIndex >= 0 && seasonIndex + 1 < seasons.length) {
      const nextSeason = seasons[seasonIndex + 1];
      const firstEp = (episodesBySeason.get(nextSeason)?.keys() ?? [])[0] as number | undefined;
      if (typeof firstEp === "number") {
        return { season: nextSeason, episode: firstEp };
      }
    }
    return null;
  }, [episodesBySeason, episodesForSeason, item, seasons, selectedEpisode, selectedSeason]);

  if (!item) return null;

  return (
    <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="max-w-5xl w-[95vw] p-0 bg-card border-border overflow-hidden gap-0">
        <DialogTitle className="sr-only">{item.title}</DialogTitle>
        <div className="relative aspect-video bg-background">
          {selectedStreamUrl ? (
            <VideoJsPlayer
              src={selectedStreamUrl}
              poster={item.backdrop}
              startTime={resumeTime}
              onTimeUpdate={(time) => {
                if (!playbackKey) return;
                if (time < 2) return;
                const last = Number(localStorage.getItem(playbackKey) ?? 0);
                if (Math.abs(time - last) < 5) return;
                localStorage.setItem(playbackKey, String(Math.floor(time)));
              }}
            />
          ) : null}
        </div>
        <div className="p-4 md:p-6">
          <h3 className="text-lg font-semibold text-foreground">
            {item.title}
          </h3>
          {item.type === "series" && seasons.length > 0 && (
            <div className="mt-3 flex flex-wrap items-end gap-3">
              <div className="min-w-[140px]">
                <p className="text-xs text-muted-foreground mb-1">
                  Temporada
                </p>
                <Select
                  value={selectedSeason != null ? String(selectedSeason) : ""}
                  onValueChange={(value) => {
                    const season = Number(value);
                    setSelectedSeason(season);
                    const firstEpisode = (episodesBySeason.get(season)?.keys() ?? [])[0] as number | undefined;
                    const episode = typeof firstEpisode === "number" ? firstEpisode : null;
                    setSelectedEpisode(episode);
                    if (episode != null) {
                      const entry = episodesBySeason.get(season)?.get(episode);
                      if (selectedAudio && entry?.[selectedAudio]) return;
                      const preferred = entry?.dublado
                        ? "dublado"
                        : entry?.legendado
                        ? "legendado"
                        : null;
                      setSelectedAudio(preferred);
                    }
                  }}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Temporada" />
                  </SelectTrigger>
                  <SelectContent>
                    {seasons.map((season) => (
                      <SelectItem key={season} value={String(season)}>
                        {season}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="min-w-[140px]">
                <p className="text-xs text-muted-foreground mb-1">Episódio</p>
                <Select
                  value={selectedEpisode != null ? String(selectedEpisode) : ""}
                  onValueChange={(value) => {
                    const episode = Number(value);
                    setSelectedEpisode(episode);
                    if (selectedSeason != null) {
                      const entry = episodesBySeason.get(selectedSeason)?.get(episode);
                      if (selectedAudio && entry?.[selectedAudio]) return;
                      const preferred = entry?.dublado
                        ? "dublado"
                        : entry?.legendado
                        ? "legendado"
                        : null;
                      setSelectedAudio(preferred);
                    }
                  }}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Episódio" />
                  </SelectTrigger>
                  <SelectContent>
                    {episodesForSeason.map((episode) => (
                      <SelectItem key={episode} value={String(episode)}>
                        {episode}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="min-w-[170px]">
                <p className="text-xs text-muted-foreground mb-1">Áudio</p>
                <Select
                  value={selectedAudio ?? ""}
                  onValueChange={(value) =>
                    setSelectedAudio(value as "legendado" | "dublado")
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Legenda/Dublado" />
                  </SelectTrigger>
                  <SelectContent>
                    {(() => {
                      const entry =
                        selectedSeason != null && selectedEpisode != null
                          ? episodesBySeason
                              .get(selectedSeason)
                              ?.get(selectedEpisode)
                          : null;
                      const options: Array<"legendado" | "dublado"> = [];
                      if (entry?.legendado) options.push("legendado");
                      if (entry?.dublado) options.push("dublado");
                      return options.map((audio) => (
                        <SelectItem key={audio} value={audio}>
                          {audio === "legendado" ? "Legendado" : "Dublado"}
                        </SelectItem>
                      ));
                    })()}
                  </SelectContent>
                </Select>
              </div>
              <Button
                type="button"
                variant="secondary"
                disabled={!nextEpisode}
                onClick={() => {
                  if (!nextEpisode) return;
                  setSelectedSeason(nextEpisode.season ?? null);
                  setSelectedEpisode(nextEpisode.episode ?? null);
                }}
              >
                Próximo episódio
              </Button>
            </div>
          )}
          {item.type !== "series" && streamOptions.length > 1 && (
            <div className="mt-3 max-w-sm">
              <p className="text-xs text-muted-foreground mb-1">
                Opção de stream
              </p>
              <Select
                value={selectedStreamUrl}
                onValueChange={setSelectedStreamUrl}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Selecione uma opção" />
                </SelectTrigger>
                <SelectContent>
                  {streamOptions.map((stream, index) => (
                    <SelectItem
                      key={`${stream.streamUrl}-${index}`}
                      value={stream.streamUrl}
                    >
                      {stream.quality && stream.groupTitle
                        ? `${stream.quality} • ${stream.groupTitle}`
                        : stream.name || stream.quality || `Opção ${index + 1}`}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          )}
          <div className="flex items-center gap-3 text-sm text-muted-foreground mt-1">
            {item.year && <span>{item.year}</span>}
            {item.rating && (
              <span className="text-primary">★ {item.rating}</span>
            )}
            {item.duration && <span>{item.duration}</span>}
            {item.type === "series" && item.seasons && (
              <span>{item.seasons} temporadas</span>
            )}
          </div>
          <p className="text-sm text-muted-foreground mt-3">
            {item.description}
          </p>
        </div>
      </DialogContent>
    </Dialog>
  );
}
