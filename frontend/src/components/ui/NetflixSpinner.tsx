import type { HTMLAttributes } from "react";

interface NetflixSpinnerProps extends HTMLAttributes<HTMLDivElement> {
  size?: number;
  label?: string;
}

export function NetflixSpinner({
  size = 36,
  label = "Carregando",
  className,
  ...props
}: NetflixSpinnerProps) {
  const dimension = `${size}px`;
  return (
    <div
      role="status"
      aria-live="polite"
      aria-label={label}
      className={className}
      {...props}
    >
      <div
        className="mx-auto rounded-full border-2 border-transparent border-t-red-500 border-r-red-500 animate-spin"
        style={{ width: dimension, height: dimension }}
      />
      <span className="sr-only">{label}</span>
    </div>
  );
}
