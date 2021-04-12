import Lenke from 'nav-frontend-lenker';
import Link from 'next/link';
import styled from 'styled-components'

import NavInfoCircle from './NavInfoCircle'

const StatusOverviewContainer = styled.div`
    max-width: 1080px;
    width: 100%;
    /* padding: 0 3rem; */
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    @media(min-width: 350px){
        padding: 0 3rem;
    }
`;

const StatusBannerWrapper = styled.div`
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
const StatusContainer = styled.div`
    padding: 2rem 1rem;
`;
const CirclesContainer = styled.div`
    max-width: 30rem;
    display: flex;
    flex-flow: row wrap;
    justify-content: space-between;
`;

const IncidentsAndStatusWrapper = styled.div`
`;

const MaintenanceStatusWrapper = styled.div`
`;

const mapStatusAndIncidentsToArray = (areas) => {
    let areasArray: Array<String> = []
    areas.map(area => {
        areasArray.push(area)
    })
    return areasArray;
}

const countServicesInAreas = (mappedAreas) => {
    let numberOfServices: number = 0;
    mappedAreas.forEach(function (area){
        numberOfServices += area.services.length
    })
    return numberOfServices
}

const countHealthyServices = (mappedAreas) => {
    let healthyServices: number = 0;
    mappedAreas.map(area => {
        healthyServices += area.services.filter(
            (service: any) => service.status !== "DOWN").length
    })
    return healthyServices
}

//TODO Create Incidents handler and UI


const StatusOverview = (props: any) => {
    let areas: Object = props.areas
    const mappedAreas: Array<String> = mapStatusAndIncidentsToArray(areas)
    const numberOfServices: number = countServicesInAreas(mappedAreas)
    const numberOfHealthyServices: number = countHealthyServices(mappedAreas)

    return (
        <StatusOverviewContainer>

            <StatusBannerWrapper>
                <div>
                    <h2>Statusmessage here</h2>
                    <span>Last updated</span>
                </div>
                <Link href="/Incidents">
                    <LenkeCustomized>
                        <span>Mer om hendelser</span>
                    </LenkeCustomized>
                </Link>
            </StatusBannerWrapper>

            <StatusContainer>
                <p>Hendelser siste 48 timene</p>
                <CirclesContainer>

                    <IncidentsAndStatusWrapper>
                        <NavInfoCircle topText="Hendelser" centerTextLeft="0" centerTextRight="16" bottomText="Siste 24 timene"/>
                    </IncidentsAndStatusWrapper>

                    <MaintenanceStatusWrapper>
                        <NavInfoCircle topText="Systemer" centerTextLeft={numberOfHealthyServices} centerTextRight={numberOfServices} bottomText="Oppe"/>
                    </MaintenanceStatusWrapper>

                </CirclesContainer>
            </StatusContainer>

        </StatusOverviewContainer>
    )
}

export default StatusOverview