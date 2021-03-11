import styled from 'styled-components'
import Layout from '../components/Layout'

// import 

const ErrorTitle = styled.h1`
    color: var(--redError);
`;

const ErrorWrapper = styled.div`
    display: flex;
    justify-content: center;
    align-items: center;
    flex-flow: column wrap;
`;

export default function Custom404() {
    return (
        <Layout>
            <ErrorWrapper>
                <ErrorTitle>404 - Page Not Found</ErrorTitle>
                <p>Denne siden eksiterer dessverre ikke :-(</p>
            </ErrorWrapper>
        </Layout>
    )
}