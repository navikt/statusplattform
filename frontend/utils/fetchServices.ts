export class ResponseError extends Error {
    public constructor (message: string, public response: Response) {
        super(message)
    }
}

export const fetchData = async (): Promise<string[]> => {
    const response = await fetch("http://localhost:3001/rest/testAreas");
    if (response.ok) {
        return response.json()
    }
    throw new ResponseError("Failed to fetch from server", response)
}