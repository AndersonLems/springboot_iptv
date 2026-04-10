import { memo } from "react";
import { cn } from "@/lib/utils";

interface AvailableBadgeProps {
  available: boolean;
}

export const AvailableBadge = memo(function AvailableBadge({
  available,
}: AvailableBadgeProps) {
  return (
    <div className="flex items-center gap-2 text-xs font-medium">
      <span
        className={cn(
          "h-2 w-2 rounded-full",
          available
            ? "bg-emerald-400 animate-pulse"
            : "bg-muted-foreground",
        )}
      />
      <span className={available ? "text-emerald-400" : "text-muted-foreground"}>
        {available ? "Disponível" : "Indisponível"}
      </span>
    </div>
  );
});
