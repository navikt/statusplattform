import { useEffect, useState } from "react";

import { PortalServiceTile } from '../components/PortalServiceTile'
import StatusOverview from '../components/StatusOverview'


import styled from 'styled-components'

const DigitalServicesContainer = styled.div`
    width: 100%;
    padding: 0;
    display: flex;
    flex-direction: column;
    align-items: center;
    @media(min-width: 550px){
        padding: 2rem 3rem;
    }
`;
const PortalServiceTileContainer = styled.div`
    width: 100%;
    flex: 1;
    display: grid;
    justify-content: center;
    grid-template-columns: repeat(1, 1fr);
    gap: 15px;
    @media (min-width: 468px){
        grid-template-columns: repeat(2, 1fr);
    }
    @media (min-width: 768px) {
        display: grid;
        grid-template-columns: repeat(auto-fit, 250px);
        grid-auto-flow: dense;
        padding: 30px;
    }
`;

const ErrorParagraph = styled.p`
    color: #ff4a4a;
    /* background-color: grey; */
    font-weight: bold;
    padding: 10px;
    border-radius: 5px;
`;

export class ResponseError extends Error {
    public constructor (message: string, public response: Response) {
        super(message)
    }
}

const fetchData = async (): Promise<string[]> => {
    const response = await fetch("http://localhost:3001/rest/testAreas");
    if (response.ok) {
        return response.json()
    }
    throw new ResponseError("Failed to fretch from server", response)
}

const DigitalServicesCards = () => {
    const [areas, setAreas] = useState([])
    const [isLoading, setIsLoading] = useState(false)

    useEffect(() => {
        (async function () {
            setIsLoading(true)
            const newAreas = await fetchData()
            // const newAreas = await AsyncFetchNavDigitalServices
            const parsedAreas = [...newAreas]
            setAreas(parsedAreas)
            setIsLoading(false)
        })()
    }, [])

    if (!areas) {
        return <ErrorParagraph>Kunne ikke hente de digitale tjenestene. Hvis problemet vedvarer, kontakt support.</ErrorParagraph>
    }

    if (isLoading) {
        return <p>Loading services...</p>
    }
    return (
        <DigitalServicesContainer>
            <StatusOverview areas={areas} />
            <PortalServiceTileContainer>
                {areas.map(area => {
                    return (
                        <PortalServiceTile key={area.name} area={area}/>
                    )
                })}
            </PortalServiceTileContainer>
        </DigitalServicesContainer>
    )
}

export default DigitalServicesCards