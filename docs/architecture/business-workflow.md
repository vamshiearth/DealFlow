# DealFlow Business Workflow

```mermaid
flowchart TD

    A["Sales Rep creates or imports Deal"]
    B{"Discount above 20%?"}
    C["Create Discount Exception"]
    D["Exception Status: PENDING"]
    E["Sales Manager reviews exception"]
    F{"Exception decision"}
    G["Exception APPROVED"]
    H["Exception REJECTED"]
    I["Sales Rep submits Deal"]
    J["Approval Engine evaluates policy"]
    K["Create required approval chain"]
    L["Sales Manager Review"]
    M{"Manager decision"}
    N["Finance Review"]
    O{"Finance decision"}
    P["VP Sales Review"]
    Q{"VP decision"}
    R["Deal APPROVED"]
    S["CHANGES_REQUESTED"]
    T["Deal REJECTED"]
    U["Sales Rep edits Deal"]
    V["Resubmit Deal"]

    A --> B
    B -->|"Yes"| C
    B -->|"No"| I
    C --> D
    D --> E
    E --> F
    F -->|"Approve"| G
    F -->|"Reject"| H
    H --> T
    G --> I
    I --> J
    J --> K
    K --> L
    L --> M
    M -->|"Approve"| N
    M -->|"Request Changes"| S
    M -->|"Reject"| T
    S --> U
    U --> V
    V --> J
    N --> O
    O -->|"Approve"| P
    O -->|"Request Changes"| S
    O -->|"Reject"| T
    P --> Q
    Q -->|"Approve"| R
    Q -->|"Request Changes"| S
    Q -->|"Reject"| T
```

## Example Workflow

A deal with:

- Deal value: $520,000
- Discount: 27%
- Margin: 13%

requires:

```text
Discount Exception
      |
Sales Manager Approval
      |
Finance Approval
      |
VP Sales Approval
      |
APPROVED
```

The deal cannot be submitted while the required discount exception is pending or rejected.

Once the exception is approved, the system creates the required sequential approval chain based on discount, margin, and deal value rules. Later approval stages cannot bypass earlier stages.
