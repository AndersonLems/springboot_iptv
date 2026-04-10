import { memo } from "react";
import { cn } from "@/lib/utils";

interface ProgressBarProps {
  progress: number;
  className?: string;
}

export const ProgressBar = memo(function ProgressBar({
  progress,
  className,
}: ProgressBarProps) {
  const safe = Math.max(0, Math.min(1, progress));
  return (
    <div className={cn("h-1 w-full rounded-full bg-muted/60", className)}>
      <div
        className="h-full rounded-full bg-emerald-400"
        style={{ width: `${safe * 100}%` }}
      />
    </div>
  );
});
