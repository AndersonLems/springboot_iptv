import { memo } from "react";
import { ChevronDown, ChevronUp } from "lucide-react";
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group";
import type { SortField, SortOrder } from "@/types/media";

interface SortToolbarProps {
  sort: SortField;
  order: SortOrder;
  onChange: (sort: SortField, order: SortOrder) => void;
}

const sortOptions: Array<{ value: SortField; label: string }> = [
  { value: "name", label: "Nome" },
  { value: "year", label: "Ano" },
  { value: "voteAverage", label: "Nota" },
  { value: "popularity", label: "Popularidade" },
];

export const SortToolbar = memo(function SortToolbar({
  sort,
  order,
  onChange,
}: SortToolbarProps) {
  return (
    <div className="flex flex-wrap items-center gap-3">
      <ToggleGroup
        type="single"
        value={sort}
        onValueChange={(value) => {
          if (!value) return;
          onChange(value as SortField, order);
        }}
        className="gap-2"
      >
        {sortOptions.map((option) => (
          <ToggleGroupItem
            key={option.value}
            value={option.value}
            className="text-xs"
          >
            {option.label}
          </ToggleGroupItem>
        ))}
      </ToggleGroup>
      <button
        onClick={() => onChange(sort, order === "asc" ? "desc" : "asc")}
        className="inline-flex items-center gap-1 text-xs text-muted-foreground hover:text-foreground transition-colors"
        aria-label="Alterar ordem"
      >
        {order === "asc" ? (
          <ChevronUp className="h-4 w-4" />
        ) : (
          <ChevronDown className="h-4 w-4" />
        )}
        {order === "asc" ? "Asc" : "Desc"}
      </button>
    </div>
  );
});
