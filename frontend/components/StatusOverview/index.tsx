import Lenke from 'nav-frontend-lenker';
import Link from 'next/link';
import styled from 'styled-components'

import NavInfoCircle from 'components/NavInfoCircle'
import MaintenanceScheduling from 'components/MaintenanceScheduling'
import { Calender } from '@navikt/ds-icons';
import { Systemtittel, Undertekst } from 'nav-frontend-typografi';
import { countHealthyServices, countServicesInAreas, mapStatusAndIncidentsToArray } from 'utils/servicesOperations';

const StatusOverviewContainer = styled.div`
    max-width: 1080px;
    width: 100%;
    padding: 0;
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    @media(min-width: 500px){
        padding: 0 3rem;
    }
`;

const StatusBannerContainer = styled.div`
    border-radius: 20px;
    background-color: white;    
    padding: 2rem 1rem;
    width: 100%;
    display: flex;
    flex-direction: column;
    div:first-child {
        padding-bottom: 1rem;
    }
    @media (min-width: 45rem) {
        display: flex;
        justify-content: space-between;
        flex-direction: row;
    }
    h2 {
        margin: 0 0 .5rem;
    }
`;
const LenkeCustomized = styled(Lenke)`
    border: 2px solid var(--navBla);
    border-radius: 15px;
    line-height: 35px;
    padding: 0 1.5rem;
    min-height: 40px;
    height: 100%;
    max-width: 184px;
    transition: 0.4s;
    display: flex;
    align-items: center;
    :hover {
        transition: 0.4s;
        background-color: var(--navBla);
        color: white;
    }
    @media(min-width: 45rem){
        align-self: center;
    }
`;
const OverviewComponents = styled.div`
    padding: 2rem 1rem;
    display: flex;
    flex-direction: column;
    @media (min-width: 350px){
        flex-direction: row;
        justify-content: space-between;
    }
`;
const StatusContainer = styled.div`
    width: calc(50% - 1rem);
    max-width: none;
`

const IconHeader = styled.div`
    width: 100%;
    margin-bottom: 2rem;
    display: flex;
    align-items: center;
    span {
        margin-right: 10px;
        font-size: 2rem;
    }
`

const CirclesContainer = styled.div`
    max-width: 30rem;
    display: flex;
    flex-flow: row wrap;
    justify-content: space-between;
`;

const IncidentsAndStatusCircleWrapper = styled.div`
    border: 2px solid transparent;
    border-radius: 50%;
`;

const MaintenanceStatusCircleWrapper = styled.div`
    border: 2px solid transparent;
    border-radius: 50%;
`;

const MaintenanceContainer = styled.div`
    width: calc(50% - 1rem);
    max-width: none;
`


//TODO Create Incidents handler and UI


const StatusOverview = (props: any) => {
    let areas: Object = props.areas
    const mappedAreas: Array<String> = mapStatusAndIncidentsToArray(areas)
    const numberOfServices: number = countServicesInAreas(mappedAreas)
    const numberOfHealthyServices: number = countHealthyServices(mappedAreas)

    return (
        <StatusOverviewContainer>

            <StatusBannerContainer>
                <div>
                    <h2>Status: Ikke implementert</h2>
                    <Undertekst>Sist oppdatert: Ikke implementert</Undertekst>
                </div>
                <Link href="/Incidents">
                    <LenkeCustomized>
                        <span>Mer om hendelser</span>
                    </LenkeCustomized>
                </Link>
            </StatusBannerContainer>
            {/*
            <OverviewComponents>
                <StatusContainer>
                    <IconHeader>
                        <span><Calender /></span>
                        <div>
                            <Systemtittel>
                                Hendelser
                                <Undertekst> siste 48 timene</Undertekst>
                            </Systemtittel>
                        </div>
                    </IconHeader>
                   <CirclesContainer>

                        <IncidentsAndStatusCircleWrapper>
                            <NavInfoCircle topText="Hendelser" centerTextLeft="0" centerTextRight="16" bottomText="Siste 24 timene"/>
                        </IncidentsAndStatusCircleWrapper>

                        <MaintenanceStatusCircleWrapper>
                            <NavInfoCircle topText="Systemer" centerTextLeft={numberOfHealthyServices} centerTextRight={numberOfServices} bottomText="Oppe"/>
                        </MaintenanceStatusCircleWrapper>

                    </CirclesContainer>
                </StatusContainer>
                <MaintenanceContainer>
                    <IconHeader>
                        <MaintenanceScheduling />
                    </IconHeader>
                </MaintenanceContainer>

            </OverviewComponents>*/}

        </StatusOverviewContainer>
    )
}

export default StatusOverview