import Lenke from 'nav-frontend-lenker';
import Link from 'next/link';
import styled from 'styled-components'

const StatusOverviewContainer = styled.div`
    max-width: 1080px;
    width: 100%;
    padding: 0 1.5rem;
    display: flex;
    flex-direction: column;
    justify-content: space-around;
`;

const StatusBannerWrapper = styled.div`
    border-radius: 10px;
    background-color: white;    
    padding: 2rem 1rem;
    width: 100%;
    @media (min-width: 45rem) {
        display: flex;
        justify-content: space-between;
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
    height: 40px;
    :hover {
        transition: 0.4s;
        background-color: var(--navBla);
        color: white;
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

const NavInfoCircleContent = styled.div`
    border: 2px solid;
    border-radius: 50%;
    min-height: 100px;
    min-width: 8.188rem;
    max-width: 14rem;
    padding: 1.5rem 0;
    text-align: center;
    > span {
        font-size: small;
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
    }
    span:nth-child(2){
        font-size: 2.5em;
    }
    @media (min-width: 560px) {
        min-width: 11.35rem;
        > span {
            line-height: 1.5rem;
            font-size: normal;
            min-height: 2.7rem;
        }
        span:nth-child(2) {
            font-size: 3.5em;
        }
    }
`;

let numberOfHealthyServices: number = 0;

const mapStatusAndIncidentsToArray = (areas) => {
    let areasArray: Array<String> = []
    areas.map(area => {
        areasArray.push(area)
    })
    return areasArray;
}
const countServicesInAreas = (mappedAreas) => {
    let numberOfServices: number = 0;
    // console.log(mappedAreas)
    mappedAreas.forEach(function (area){
        numberOfServices += area.services.length
    })
    mappedAreas.map(area => {
        numberOfHealthyServices += area.services.filter(
            (service: any) => service.status !== "DOWN").length
    })
    return numberOfServices
}

//TODO Create Incidents handler and UI


const StatusOverview = (props: any) => {
    let areas: Object = props.areas
    const mappedAreas: Array<String> = mapStatusAndIncidentsToArray(areas)
    const numberOfServices: number = countServicesInAreas(mappedAreas)

    return (
        <StatusOverviewContainer>

            <StatusBannerWrapper>
                <div>
                    <h2>Statusmessage here</h2>
                    <span>Last updated</span>
                </div>
                <Link href="/detailed-incidents">
                    <LenkeCustomized>
                        <span>Mer om hendelser</span>
                    </LenkeCustomized>
                </Link>
            </StatusBannerWrapper>

            <StatusContainer>
                <p>Hendelser siste 48 timene</p>
                <CirclesContainer>

                    <IncidentsAndStatusWrapper>
                        <NavInfoCircleContent>
                            <span>Hendelser</span>
                            <span>0/16</span>
                            <span>Siste 24 timene</span>
                        </NavInfoCircleContent>
                    </IncidentsAndStatusWrapper>

                    <MaintenanceStatusWrapper>
                        <NavInfoCircleContent>
                            <span>Systemer</span>
                            <span>{numberOfHealthyServices}/{numberOfServices}</span>
                            <span>Oppe</span>
                        </NavInfoCircleContent>
                    </MaintenanceStatusWrapper>

                </CirclesContainer>
            </StatusContainer>

        </StatusOverviewContainer>
    )
}

export default StatusOverview