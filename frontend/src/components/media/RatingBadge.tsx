import { memo } from "react";
import { Star } from "lucide-react";
import { cn } from "@/lib/utils";

interface RatingBadgeProps {
  rating: number;
}

export const RatingBadge = memo(function RatingBadge({ rating }: RatingBadgeProps) {
  const colorClass = rating >= 7 ? "text-green-400" : rating >= 5 ? "text-yellow-400" : "text-red-400";

  return (
    <div className={cn("flex items-center gap-1 text-xs font-semibold", colorClass)}>
      <Star className="h-3.5 w-3.5 fill-current" />
      <span>{rating.toFixed(1)}</span>
    </div>
  );
});
