export interface ISource {
    id: SourceType,
    title: string
    content?: React.ReactNode,
    text: string,
}

export enum SourceType {
    phone = "phone",
    email = "email",
    slack = "slack",
    close = "close"
}