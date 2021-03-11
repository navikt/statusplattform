import { useEffect, useState } from "react";
import {PortalServiceTile} from '../components/PortalServiceTile'

import Panel from 'nav-frontend-paneler';
import styled from 'styled-components'

import { Bag, Folder, PensionBag, HealthCase, ErrorFilled, WarningFilled, Employer, Information, People, Family, Service, Globe } from '@navikt/ds-icons'
import { Undertittel } from "nav-frontend-typografi";

const DigitalServicesContainer = styled.div`
    width: 100%;
    padding: 30px;
    flex: 1;
    display: grid;
    justify-content: center;
    grid-template-columns: repeat(1, 1fr);
    gap: 15px;
    @media (min-width: 250px){

    }
    @media (min-width: 768px) {
        display: grid;
        grid-template-columns: repeat(auto-fill, 250px);
    }
`;



async function fetchData() {
    try {
        const response = await fetch("http://localhost:3001/rest/testAreas");
        if (response.ok) {
            const data = await response.json()
            return data
        }
        else {
            throw Error(`Request rejected with error code:  + ${response.status}`)
        }
    } catch (error) {
        console.error("Error is: " + error)
    }
}

const DigitalServicesLandingPage = () => {
    const [areas, setAreas] = useState([])
    const [isLoading, setIsLoading] = useState(false)

    useEffect(() => {
        (async function () {
            setIsLoading(true)
            const newAreas = await fetchData()
            // const newAreas = await AsyncFetchNavDigitalServices
            setAreas(newAreas)
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
            {areas.map(area => {
                return (
                    <PortalServiceTile key={area.name} area={area}/>
                )
            })}
        </DigitalServicesContainer>
    )
}

export default DigitalServicesLandingPage