/** @type {import('next').NextConfig} */
const GATEWAY_URL = process.env.GATEWAY_URL || "http://localhost:8080";

const nextConfig = {
  transpilePackages: ["@itsh/auth", "@itsh/api-client"],
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: `${GATEWAY_URL}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
