# Adopt RFC 6648 for Custom HTTP Headers
Status: Accepted

## Context

In the development of the Helpdesk application, we require custom HTTP headers to pass application-specific metadata (such as a Request ID for log tracing) between the frontend, the API gateway, and the backend services. Historically, the industry convention was to prefix non-standard or experimental headers with X- (e.g., X-Request-Id). However, the IETF published RFC 6648, officially deprecating the use of the X- prefix. The motivation behind this standard is that experimental headers often become widely adopted de facto standards; when they are eventually standardized, removing the X- prefix breaks backward compatibility, while keeping it permanently attaches an "experimental" label to a stable standard.

## Decision

We will strictly adhere to RFC 6648 for all internal and newly created custom HTTP headers. Specifically, we will not prepend X- or x- to any newly minted custom headers. Instead, we will use clear, domain-specific names (e.g., Request-Id instead of X-Request-Id). For external infrastructure that we do not control (such as cloud load balancers or third-party APIs) that automatically inject legacy X- headers, our edge gateway will accept them to maintain interoperability, but our internal services will map and propagate them using compliant names.

## Consequences

What becomes easier:

- Standards Compliance: Our API contracts natively align with modern web standards and IETF best practices.
  
- Future-Proofing: If any of our custom headers are later adopted by wider standards, the migration path is seamless since the naming is already clean.

- Readability: Header names are more semantic and easier to read without the redundant experimental prefix.

What becomes more difficult:

- Integration Friction: Older third-party libraries, legacy proxies, or external developers consuming our API might expect X- prefixes out of habit, requiring us to provide very clear API documentation.

- Edge Mapping Overhead: We must write and maintain specific mapping logic at our API Gateway or Reverse Proxy to translate incoming legacy X- headers from external systems into our internal compliant headers.

