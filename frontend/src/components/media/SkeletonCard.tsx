import { memo } from "react";

export const SkeletonCard = memo(function SkeletonCard() {
  return (
    <div className="aspect-[2/3] rounded-lg bg-muted/40 skeleton-shimmer" />
  );
});
