/**
 * Simple logger that avoids leaking error details in production.
 * In development, logs normally; in production, only short messages (no stack/object dumps).
 */
const isProd = import.meta.env.PROD;

export const logger = {
  error: (message, err) => {
    if (isProd) {
      // Production: avoid exposing stack traces or response data
      if (typeof message === "string") console.error(message);
      else console.error("Error");
    } else {
      console.error(message, err);
    }
  },
  warn: (...args) => {
    if (!isProd) console.warn(...args);
  },
  log: (...args) => {
    if (!isProd) console.log(...args);
  },
};
