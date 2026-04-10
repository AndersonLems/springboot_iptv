import { memo } from "react";
import { Badge } from "@/components/ui/badge";
import { Switch } from "@/components/ui/switch";
import type { FilterState } from "@/types/media";
import { cn } from "@/lib/utils";

interface FilterPanelProps {
  filters: FilterState;
  onChange: (next: FilterState) => void;
  genres: Array<{ id: number; name: string }>;
  open: boolean;
  onToggle: () => void;
}

export const FilterPanel = memo(function FilterPanel({
  filters,
  onChange,
  genres,
  open,
  onToggle,
}: FilterPanelProps) {
  const currentYear = new Date().getFullYear();

  const update = (partial: Partial<FilterState>) => {
    onChange({ ...filters, ...partial });
  };

  const toggleGenre = (id: number) => {
    if (filters.genres.includes(id)) {
      update({ genres: filters.genres.filter((g) => g !== id) });
    } else {
      update({ genres: [...filters.genres, id] });
    }
  };

  return (
    <div className="mb-6">
      <div
        className={cn(
          "overflow-hidden transition-[max-height] duration-300",
          open ? "max-h-[520px]" : "max-h-0",
        )}
      >
        <div className="mt-4 grid gap-5 rounded-lg border border-border bg-card p-4">
          <div className="grid gap-3">
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium">Ano mínimo</span>
              <span className="text-xs text-muted-foreground">
                {filters.yearMin}
              </span>
            </div>
            <input
              type="range"
              min={1950}
              max={currentYear}
              value={filters.yearMin}
              onChange={(event) =>
                update({ yearMin: Number(event.target.value) })
              }
              className="w-full"
            />
          </div>

          <div className="grid gap-3">
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium">Ano máximo</span>
              <span className="text-xs text-muted-foreground">
                {filters.yearMax}
              </span>
            </div>
            <input
              type="range"
              min={1950}
              max={currentYear}
              value={filters.yearMax}
              onChange={(event) =>
                update({ yearMax: Number(event.target.value) })
              }
              className="w-full"
            />
          </div>

          <div className="grid gap-3">
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium">Nota mínima</span>
              <span className="text-xs text-muted-foreground">
                {filters.minRating.toFixed(1)}
              </span>
            </div>
            <input
              type="range"
              min={0}
              max={10}
              step={0.5}
              value={filters.minRating}
              onChange={(event) =>
                update({ minRating: Number(event.target.value) })
              }
              className="w-full"
            />
          </div>

          <div className="flex items-center justify-between">
            <span className="text-sm font-medium">Somente disponíveis</span>
            <Switch
              checked={filters.availableOnly}
              onCheckedChange={(checked) => update({ availableOnly: checked })}
            />
          </div>

          {genres.length > 0 && (
            <div className="grid gap-3">
              <span className="text-sm font-medium">Gêneros</span>
              <div className="flex flex-wrap gap-2">
                {genres.map((genre) => {
                  const active = filters.genres.includes(genre.id);
                  return (
                    <Badge
                      key={genre.id}
                      role="button"
                      tabIndex={0}
                      aria-pressed={active}
                      onClick={() => toggleGenre(genre.id)}
                      className={cn(
                        "cursor-pointer select-none",
                        active
                          ? "bg-primary text-primary-foreground"
                          : "bg-secondary text-secondary-foreground",
                      )}
                    >
                      {genre.name}
                    </Badge>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
});
