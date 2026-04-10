import { useEffect } from "react";
import type { RefObject } from "react";

export function useInfiniteScroll(
  ref: RefObject<Element | null>,
  callback: () => void,
  enabled: boolean,
) {
  useEffect(() => {
    if (!enabled || !ref.current) return;
    const element = ref.current;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0]?.isIntersecting) {
          callback();
        }
      },
      { rootMargin: "200px" },
    );

    observer.observe(element);
    return () => observer.disconnect();
  }, [callback, enabled, ref]);
}
