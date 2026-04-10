import { memo } from "react";

export const LiveBadge = memo(function LiveBadge() {
  return (
    <div className="flex items-center gap-2 text-xs font-semibold text-red-500">
      <span className="h-2 w-2 rounded-full bg-red-500 live-pulse" />
      <span>AO VIVO</span>
    </div>
  );
});
