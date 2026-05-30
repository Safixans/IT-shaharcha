/** @type {import('next').NextConfig} */
const GATEWAY_URL = process.env.GATEWAY_URL || "http://localhost:8080";

const nextConfig = {
  // Consume the workspace TS packages directly (no separate build step).
  transpilePackages: ["@itsh/auth", "@itsh/api-client"],
  async rewrites() {
    // Proxy browser /api/* calls to the gateway so requests are same-origin
    // (no CORS) and tokens flow straight through.
    return [
      {
        source: "/api/:path*",
        destination: `${GATEWAY_URL}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
